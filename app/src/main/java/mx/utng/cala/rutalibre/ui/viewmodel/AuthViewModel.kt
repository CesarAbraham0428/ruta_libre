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

    fun register(nombre: String, usuario: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, registrationSuccess = false)
            repository.register(nombre, usuario, password).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(isLoading = false, registrationSuccess = true) },
                onFailure = { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetRegistrationState() {
        _uiState.value = _uiState.value.copy(registrationSuccess = false)
    }

    fun actualizarNombreLocal(nuevoNombre: String) {
        _uiState.value = _uiState.value.copy(nombre = nuevoNombre)
        persistSession(_uiState.value)
    }

    fun actualizarPesoLocal(nuevoPesoKg: Double) {
        _uiState.value = _uiState.value.copy(pesoKg = nuevoPesoKg)
        persistSession(_uiState.value)
    }

    fun cerrarSesion() {
        sessionStore.clear()
        _uiState.value = AuthUiState()
    }

    private fun persistSession(state: AuthUiState) {
        val idUsuario = state.idUsuario ?: return
        val nombre = state.nombre ?: return
        val token = state.token ?: return
        sessionStore.save(AuthSession(idUsuario, nombre, state.pesoKg, token))
    }
}
