package mx.utng.cala.rutalibre.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.utng.cala.core.data.repository.AuthRepository
import mx.utng.cala.rutalibre.data.auth.AuthSession
import mx.utng.cala.rutalibre.data.auth.AuthSessionStore

/** Estado de autenticación que consume la interfaz móvil. */
data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val idUsuario: Int? = null,
    val nombre: String? = null,
    val pesoKg: Double? = null,
    val token: String? = null,
    val registrationSuccess: Boolean = false,
    val error: String? = null
)

/** Coordina inicio de sesión, registro y persistencia de la cuenta activa. */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository()
    private val sessionStore = AuthSessionStore(application)
    private val _uiState = MutableStateFlow(
        sessionStore.restore()?.let {
            AuthUiState(
                isLoggedIn = true,
                idUsuario = it.idUsuario,
                nombre = it.nombre,
                pesoKg = it.pesoKg,
                token = it.token
            )
        } ?: AuthUiState()
    )
    val uiState: StateFlow<AuthUiState> = _uiState

    /** Autentica al usuario y guarda el token recibido si las credenciales son válidas. */
    fun login(usuario: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.login(usuario, password).fold(
                onSuccess = {
                    val loggedInState = AuthUiState(
                        isLoading = false, 
                        isLoggedIn = true,
                        idUsuario = it.idUsuario,
                        nombre = it.nombre,
                        pesoKg = it.pesoKg,
                        token = it.token
                    )
                    _uiState.value = loggedInState
                    persistSession(loggedInState)
                },
                onFailure = { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) }
            )
        }
    }

    /** Registra una cuenta nueva y notifica a la pantalla el resultado de la operación. */
    fun register(nombre: String, usuario: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, registrationSuccess = false)
            repository.register(nombre, usuario, password).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(isLoading = false, registrationSuccess = true) },
                onFailure = { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) }
            )
        }
    }

    /** Limpia el mensaje de error mostrado por la interfaz. */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /** Restablece la bandera de registro exitoso después de consumirla. */
    fun resetRegistrationState() {
        _uiState.value = _uiState.value.copy(registrationSuccess = false)
    }

    /** Actualiza el nombre local y sincroniza el cambio con la sesión persistida. */
    fun actualizarNombreLocal(nuevoNombre: String) {
        _uiState.value = _uiState.value.copy(nombre = nuevoNombre)
        persistSession(_uiState.value)
    }

    /** Actualiza el peso local y lo conserva junto con la sesión autenticada. */
    fun actualizarPesoLocal(nuevoPesoKg: Double) {
        _uiState.value = _uiState.value.copy(pesoKg = nuevoPesoKg)
        persistSession(_uiState.value)
    }

    /** Borra la sesión local y devuelve el estado de autenticación a su valor inicial. */
    fun cerrarSesion() {
        sessionStore.clear()
        _uiState.value = AuthUiState()
    }

    /** Persiste una sesión solo cuando contiene identificador, nombre y token válidos. */
    private fun persistSession(state: AuthUiState) {
        val idUsuario = state.idUsuario ?: return
        val nombre = state.nombre ?: return
        val token = state.token ?: return
        sessionStore.save(AuthSession(idUsuario, nombre, state.pesoKg, token))
    }
}
