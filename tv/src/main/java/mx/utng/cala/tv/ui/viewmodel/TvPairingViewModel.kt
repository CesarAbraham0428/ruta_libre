package mx.utng.cala.tv.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
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
    private val _uiState = MutableStateFlow(
        TvPairingUiState(cargando = store.idUsuario == null, idUsuario = store.idUsuario)
    )
    val uiState: StateFlow<TvPairingUiState> = _uiState

    init {
        if (store.idUsuario == null || store.token == null) solicitarCodigo() else validarSesionGuardada()
    }

    /** Comprueba periodicamente que el token guardado siga vigente. */
    private fun validarSesionGuardada() {
        viewModelScope.launch {
            while (isActive && _uiState.value.idUsuario != null) {
                val token = store.token ?: return@launch
                val result = repository.validarSesionDispositivo(token)
                val statusError = result.exceptionOrNull()?.message.orEmpty()
                if (result.isFailure && ("(401)" in statusError || "(403)" in statusError)) {
                    limpiarSesionYGenerarCodigo()
                    return@launch
                }
                delay(5_000)
            }
        }
    }

    /** Borra la sesion local y reinicia el proceso de vinculacion. */
    private fun limpiarSesionYGenerarCodigo() {
        if (_uiState.value.idUsuario == null && _uiState.value.cargando) return
        store.clear()
        _uiState.value = TvPairingUiState(cargando = true)
        solicitarCodigo()
    }

    /** Solicita un codigo temporal y comienza a esperar su autorizacion. */
    fun solicitarCodigo() {
        viewModelScope.launch {
            _uiState.value = TvPairingUiState(cargando = true)
            repository.solicitarTv("Ruta Libre TV").fold(
                onSuccess = { solicitud ->
                    _uiState.value = TvPairingUiState(cargando = false, codigo = solicitud.codigo)
                    esperarAutorizacion(solicitud.idDispositivo, solicitud.secreto)
                },
                onFailure = { error ->
                    _uiState.value = TvPairingUiState(cargando = false, error = error.message)
                }
            )
        }
    }

    /** Consulta hasta que el codigo sea vinculado o expire. */
    private suspend fun esperarAutorizacion(idDispositivo: String, secreto: String) {
        while (viewModelScope.isActive && _uiState.value.idUsuario == null) {
            delay(2_500)
            repository.consultarEstado(idDispositivo, secreto).fold(
                onSuccess = { estado ->
                    when (estado.estado) {
                        "vinculado" -> {
                            val idUsuario = estado.idUsuario
                            val token = estado.token
                            if (idUsuario != null && token != null) {
                                store.save(idUsuario, idDispositivo, token)
                                _uiState.value = TvPairingUiState(cargando = false, idUsuario = idUsuario)
                                return
                            }
                        }
                        "expirado" -> {
                            _uiState.value = TvPairingUiState(
                                cargando = false,
                                error = "El código expiró. Genera uno nuevo."
                            )
                            return
                        }
                    }
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                    return
                }
            )
        }
    }

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
}
