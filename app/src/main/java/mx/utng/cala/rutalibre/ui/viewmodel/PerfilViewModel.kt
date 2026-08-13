package mx.utng.cala.rutalibre.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.utng.cala.core.data.repository.RepositorioUsuario

/** Estado de consulta y actualización de los datos del perfil. */
data class EstadoUiPerfil(
    val cargando: Boolean = false,
    val nombreUsuario: String = "",
    val nombre: String = "",
    val pesoKg: Double? = null,
    val exito: Boolean = false,
    val error: String? = null
)

/** Gestiona la carga y edición de nombre, contraseña y peso del usuario. */
class PerfilViewModel : ViewModel() {

    private val repositorio = RepositorioUsuario()
    private val _estado = MutableStateFlow(EstadoUiPerfil())
    val estado: StateFlow<EstadoUiPerfil> = _estado

    /** Obtiene los datos actuales del usuario desde el backend. */
    fun cargarUsuario(idUsuario: Int) {
        viewModelScope.launch {
            _estado.value = _estado.value.copy(cargando = true, error = null)
            repositorio.obtenerUsuario(idUsuario).fold(
                onSuccess = { usuario ->
                    _estado.value = _estado.value.copy(
                        cargando = false,
                        nombreUsuario = usuario.nombreUsuario,
                        nombre = usuario.nombre,
                        pesoKg = usuario.pesoKg
                    )
                },
                onFailure = { error ->
                    _estado.value = _estado.value.copy(
                        cargando = false,
                        error = error.message
                    )
                }
            )
        }
    }

    /** Guarda los cambios del perfil y actualiza el estado mostrado por la pantalla. */
    fun actualizarUsuario(
        idUsuario: Int,
        nuevoNombre: String,
        nuevaContrasena: String?,
        nuevoPesoKg: Double
    ) {
        viewModelScope.launch {
            _estado.value = _estado.value.copy(cargando = true, error = null, exito = false)
            repositorio.actualizarUsuario(idUsuario, nuevoNombre, nuevaContrasena, nuevoPesoKg).fold(
                onSuccess = {
                    _estado.value = _estado.value.copy(
                        cargando = false,
                        nombre = nuevoNombre,
                        pesoKg = nuevoPesoKg,
                        exito = true
                    )
                },
                onFailure = { error ->
                    _estado.value = _estado.value.copy(
                        cargando = false,
                        error = error.message
                    )
                }
            )
        }
    }

    /** Consume la confirmación de actualización exitosa del perfil. */
    fun restablecerExito() {
        _estado.value = _estado.value.copy(exito = false)
    }

    /** Limpia el mensaje de error del perfil. */
    fun limpiarError() {
        _estado.value = _estado.value.copy(error = null)
    }
}
