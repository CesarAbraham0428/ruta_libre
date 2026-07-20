package mx.utng.cala.rutalibre.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.utng.cala.core.data.dto.response.GrupoResponse
import mx.utng.cala.core.data.dto.response.MiembroGrupoResponse
import mx.utng.cala.core.data.repository.GrupoRepository

data class EstadoUiGrupos(
    val listaGrupos: List<GrupoResponse> = emptyList(),
    val listaMiembros: List<MiembroGrupoResponse> = emptyList(),
    val listaRanking: List<MiembroGrupoResponse> = emptyList(),
    val estaCargando: Boolean = false,
    val mensajeError: String? = null
)

class GrupoViewModel : ViewModel() {

    private val repository = GrupoRepository()
    private val _estadoUi = MutableStateFlow(EstadoUiGrupos())
    val estadoUi: StateFlow<EstadoUiGrupos> = _estadoUi

    fun cargarGruposDeUsuario(idUsuario: Int) {
        viewModelScope.launch {
            _estadoUi.value = _estadoUi.value.copy(estaCargando = true, mensajeError = null)
            repository.getGrupos(idUsuario).fold(
                onSuccess = { grupos ->
                    _estadoUi.value = _estadoUi.value.copy(
                        listaGrupos = grupos,
                        estaCargando = false,
                        mensajeError = null
                    )
                },
                onFailure = { error -> mostrarError(error) }
            )
        }
    }

    fun crearNuevoGrupo(nombre: String, descripcion: String?, idCreador: Int) {
        viewModelScope.launch {
            _estadoUi.value = _estadoUi.value.copy(estaCargando = true, mensajeError = null)
            repository.crearGrupo(nombre, descripcion, idCreador).fold(
                onSuccess = { cargarGruposDeUsuario(idCreador) },
                onFailure = { error -> mostrarError(error) }
            )
        }
    }

    fun unirseAGrupoConCodigo(idUsuario: Int, codigo: String) {
        viewModelScope.launch {
            _estadoUi.value = _estadoUi.value.copy(estaCargando = true, mensajeError = null)
            repository.unirseGrupo(idUsuario, codigo).fold(
                onSuccess = { cargarGruposDeUsuario(idUsuario) },
                onFailure = { error -> mostrarError(error) }
            )
        }
    }

    fun cargarDetalleGrupo(idGrupo: Int) {
        viewModelScope.launch {
            _estadoUi.value = _estadoUi.value.copy(
                estaCargando = true,
                mensajeError = null,
                listaMiembros = emptyList(),
                listaRanking = emptyList()
            )

            val miembrosDeferred = async { repository.getMiembros(idGrupo) }
            val rankingDeferred = async { repository.getRanking(idGrupo) }
            val miembrosResult = miembrosDeferred.await()
            val rankingResult = rankingDeferred.await()
            val miembros = miembrosResult.getOrNull()
            val ranking = rankingResult.getOrNull()
            val error = listOf(miembrosResult, rankingResult).firstNotNullOfOrNull { it.exceptionOrNull() }

            _estadoUi.value = _estadoUi.value.copy(
                listaMiembros = miembros ?: emptyList(),
                listaRanking = ranking?.miembros ?: emptyList(),
                estaCargando = false,
                mensajeError = error?.message
            )
        }
    }

    fun salirDeGrupo(idUsuario: Int, idGrupo: Int, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _estadoUi.value = _estadoUi.value.copy(estaCargando = true, mensajeError = null)
            repository.salirDeGrupo(idUsuario, idGrupo).fold(
                onSuccess = {
                    _estadoUi.value = _estadoUi.value.copy(
                        listaGrupos = _estadoUi.value.listaGrupos.filter { it.idGrupo != idGrupo },
                        estaCargando = false
                    )
                    onSuccess()
                },
                onFailure = { error -> mostrarError(error) }
            )
        }
    }

    fun limpiarError() {
        _estadoUi.value = _estadoUi.value.copy(mensajeError = null)
    }

    private fun mostrarError(error: Throwable) {
        _estadoUi.value = _estadoUi.value.copy(
            estaCargando = false,
            mensajeError = error.message ?: "Ocurrió un error al procesar el grupo"
        )
    }
}
