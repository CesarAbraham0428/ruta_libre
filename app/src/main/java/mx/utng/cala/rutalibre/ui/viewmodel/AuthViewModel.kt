package mx.utng.cala.rutalibre.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.utng.cala.core.data.repository.AuthRepository

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val registrationSuccess: Boolean = false,
    val error: String? = null
)

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun login(usuario: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.login(usuario, password).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(isLoading = false, isLoggedIn = true) },
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
}
