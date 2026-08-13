package mx.utng.cala.tv.ui.viewmodel

import android.app.Application
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import mx.utng.cala.core.data.remote.ApiException
import mx.utng.cala.core.data.repository.DispositivoRepository
import mx.utng.cala.tv.data.TvIdentityStore

/** Estado observable del codigo y la sesion vinculada de la TV. */
data class TvPairingUiState(
    val cargando: Boolean = true,
    val codigo: String? = null,
    val idUsuario: Int? = null,
    val error: String? = null
)

/** Controla la vinculacion, validacion y cierre de sesion de la TV. */
class TvPairingViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DispositivoRepository()
    private val store = TvIdentityStore(application)
    private val connectivityManager = application.getSystemService(ConnectivityManager::class.java)
    private val _uiState = MutableStateFlow(
        TvPairingUiState(cargando = store.idUsuario == null, idUsuario = store.idUsuario)
    )
    val uiState: StateFlow<TvPairingUiState> = _uiState

    private var pairingJob: Job? = null
    private var validationJob: Job? = null

    init {
        if (store.idUsuario == null || store.token == null) solicitarCodigo() else validarSesionGuardada()
    }

    /** Comprueba periodicamente que el token guardado siga vigente. */
    private fun validarSesionGuardada() {
        validationJob?.cancel()
        validationJob = viewModelScope.launch {
            while (isActive && _uiState.value.idUsuario != null) {
                val token = store.token ?: return@launch
                val result = repository.validarSesionDispositivo(token)
                val error = result.exceptionOrNull()
                if (error is ApiException && error.statusCode in setOf(401, 403)) {
                    limpiarSesionYGenerarCodigo()
                    return@launch
                }
                delay(5_000)
            }
        }
    }

    /** Borra la sesion local y reinicia el proceso de vinculacion. */
    private fun limpiarSesionYGenerarCodigo() {
        pairingJob?.cancel()
        validationJob?.cancel()
        store.clear()
        _uiState.value = TvPairingUiState(cargando = true)
        solicitarCodigo()
    }

    /** Solicita un codigo temporal y comienza a esperar su autorizacion. */
    fun solicitarCodigo() {
        if (_uiState.value.idUsuario != null) return
        pairingJob?.cancel()
        pairingJob = viewModelScope.launch { solicitarCodigoConReintentos() }
    }

    /** Genera el código y se recupera automáticamente de cortes breves o de un arranque lento de Render. */
    private suspend fun solicitarCodigoConReintentos() {
        _uiState.value = TvPairingUiState(cargando = true)
        var intentos = 0

        while (currentCoroutineContext().isActive) {
            esperarConexion()
            _uiState.value = _uiState.value.copy(
                cargando = true,
                error = null
            )

            val result = repository.solicitarTv("Ruta Libre TV")
            val solicitud = result.getOrNull()
            if (solicitud != null) {
                _uiState.value = TvPairingUiState(
                    cargando = false,
                    codigo = solicitud.codigo
                )
                esperarAutorizacion(solicitud.idDispositivo, solicitud.secreto)
                return
            }

            val error = result.exceptionOrNull() ?: IOException("Fallo de red desconocido")
            if (!esErrorTransitorio(error)) {
                _uiState.value = TvPairingUiState(
                    cargando = false,
                    error = mensajeAmigable(error)
                )
                return
            }

            intentos += 1
            val espera = esperaReintento(intentos)
            _uiState.value = TvPairingUiState(
                cargando = true,
                error = mensajeAmigable(error)
            )
            delay(espera)
        }
    }

    /** Consulta hasta que el código sea vinculado o expire; un microcorte no invalida el código visible. */
    private suspend fun esperarAutorizacion(idDispositivo: String, secreto: String) {
        var fallosConsecutivos = 0

        while (currentCoroutineContext().isActive && _uiState.value.idUsuario == null) {
            delay(if (fallosConsecutivos == 0) 2_500 else esperaReintento(fallosConsecutivos))
            esperarConexion()

            val result = repository.consultarEstado(idDispositivo, secreto)
            val estado = result.getOrNull()
            if (estado != null) {
                fallosConsecutivos = 0
                _uiState.value = _uiState.value.copy(error = null)
                when (estado.estado) {
                    "vinculado" -> {
                        val idUsuario = estado.idUsuario
                        val token = estado.token
                        if (idUsuario != null && token != null) {
                            store.save(idUsuario, idDispositivo, token)
                            _uiState.value = TvPairingUiState(cargando = false, idUsuario = idUsuario)
                            validarSesionGuardada()
                            return
                        }
                    }

                    "expirado" -> {
                        _uiState.value = _uiState.value.copy(
                            cargando = false,
                            error = "El código expiró. Genera uno nuevo."
                        )
                        return
                    }
                }
                continue
            }

            val error = result.exceptionOrNull() ?: IOException("Fallo de red desconocido")
            if (!esErrorTransitorio(error)) {
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = mensajeAmigable(error)
                )
                return
            }

            fallosConsecutivos += 1
            _uiState.value = _uiState.value.copy(
                cargando = false,
                error = mensajeAmigable(error)
            )
        }
    }

    /** Evita iniciar llamadas costosas mientras Android informa que no existe una red disponible. */
    private suspend fun esperarConexion() {
        while (currentCoroutineContext().isActive && !hayConexion()) {
            _uiState.value = _uiState.value.copy(
                cargando = _uiState.value.codigo == null,
                error = "La TV no tiene conexión a Internet."
            )
            delay(2_000)
        }
    }

    /**
     * Comprueba que Android tenga una red activa con acceso a Internet validado.
     * Esto evita solicitar el código durante los primeros segundos del arranque del
     * emulador, cuando la interfaz Ethernet existe pero DNS todavía no está disponible.
     */
    private fun hayConexion(): Boolean = try {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    } catch (_: SecurityException) {
        true
    }

    /** Determina si un error puede resolverse reintentando sin intervención del usuario. */
    private fun esErrorTransitorio(error: Throwable): Boolean = when (error) {
        is IOException -> true
        is ApiException -> error.statusCode in TRANSIENT_HTTP_CODES
        else -> false
    }

    /** Traduce fallos técnicos de red o HTTP a mensajes breves para la pantalla TV. */
    private fun mensajeAmigable(error: Throwable): String = when (error) {
        is UnknownHostException -> "No se pudo encontrar el servidor. Revisa la conexión Wi-Fi."
        is SocketTimeoutException -> "La conexión está tardando en responder."
        is IOException -> "La conexión se interrumpió. Se volverá a intentar."
        is ApiException -> when (error.statusCode) {
            502, 503, 504 -> "El servicio no está disponible temporalmente."
            408, 425, 429 -> "No fue posible conectar. Se volverá a intentar."
            else -> error.message ?: "No se pudo completar la vinculación."
        }

        else -> error.message ?: "No se pudo completar la vinculación."
    }

    /** Calcula el tiempo de espera con retroceso exponencial y un límite de 15 segundos. */
    private fun esperaReintento(intento: Int): Long =
        (2_000L * (1L shl (intento - 1).coerceIn(0, 3))).coerceAtMost(15_000L)

    /** Cierra la sesion actual en el backend y reinicia la vinculacion. */
    fun cerrarSesion() {
        viewModelScope.launch {
            store.token?.let { repository.cerrarSesionDispositivo(it) }
            limpiarSesionYGenerarCodigo()
        }
    }

    /** Atiende una orden MQTT de cierre de sesion remoto. */
    fun cerrarSesionRemota() {
        limpiarSesionYGenerarCodigo()
    }

    /** Códigos HTTP que suelen representar indisponibilidad temporal del servicio. */
    private companion object {
        val TRANSIENT_HTTP_CODES = setOf(408, 425, 429, 500, 502, 503, 504)
    }
}
