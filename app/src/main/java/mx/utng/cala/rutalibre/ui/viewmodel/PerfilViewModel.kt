package mx.utng.cala.rutalibre.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.utng.cala.core.data.repository.RepositorioUsuario

data class EstadoUiPerfil(
    val cargando: Boolean = false,
    val nombreUsuario: String = "",
    val nombre: String = "",
    val pesoKg: Double? = null,
    val exito: Boolean = false,
    val error: String? = null
)

class PerfilViewModel : ViewModel() {

    private val repositorio = RepositorioUsuario()
    private val _estado = MutableStateFlow(EstadoUiPerfil())
    val estado: StateFlow<EstadoUiPerfil> = _estado

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

    fun restablecerExito() {
        _estado.value = _estado.value.copy(exito = false)
    }

    fun limpiarError() {
        _estado.value = _estado.value.copy(error = null)
    }
}
