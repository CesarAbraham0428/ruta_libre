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

/** Estado de grupos, miembros, ranking y operaciones en curso. */
data class EstadoUiGrupos(
    val listaGrupos: List<GrupoResponse> = emptyList(),
    val listaMiembros: List<MiembroGrupoResponse> = emptyList(),
    val listaRanking: List<MiembroGrupoResponse> = emptyList(),
    val estaCargando: Boolean = false,
    val mensajeError: String? = null
)

/** Coordina la consulta, creación, unión y salida de grupos deportivos. */
class GrupoViewModel : ViewModel() {

    private val repository = GrupoRepository()
    private val _estadoUi = MutableStateFlow(EstadoUiGrupos())
    val estadoUi: StateFlow<EstadoUiGrupos> = _estadoUi

    /** Carga los grupos a los que pertenece el usuario. */
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

    /** Crea un grupo y vuelve a cargar la lista del creador. */
    fun crearNuevoGrupo(nombre: String, descripcion: String?, idCreador: Int) {
        viewModelScope.launch {
            _estadoUi.value = _estadoUi.value.copy(estaCargando = true, mensajeError = null)
            repository.crearGrupo(nombre, descripcion, idCreador).fold(
                onSuccess = { cargarGruposDeUsuario(idCreador) },
                onFailure = { error -> mostrarError(error) }
            )
        }
    }

    /** Une al usuario a un grupo mediante su código de invitación. */
    fun unirseAGrupoConCodigo(idUsuario: Int, codigo: String) {
        viewModelScope.launch {
            _estadoUi.value = _estadoUi.value.copy(estaCargando = true, mensajeError = null)
            repository.unirseGrupo(idUsuario, codigo).fold(
                onSuccess = { cargarGruposDeUsuario(idUsuario) },
                onFailure = { error -> mostrarError(error) }
            )
        }
    }

    /** Obtiene en paralelo los miembros y el ranking de un grupo. */
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

    /** Retira al usuario del grupo y ejecuta una acción posterior si tiene éxito. */
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

    /** Limpia el error de la operación de grupos actualmente visible. */
    fun limpiarError() {
        _estadoUi.value = _estadoUi.value.copy(mensajeError = null)
    }

    /** Convierte una excepción en un estado de error comprensible para la UI. */
    private fun mostrarError(error: Throwable) {
        _estadoUi.value = _estadoUi.value.copy(
            estaCargando = false,
            mensajeError = error.message ?: "Ocurrió un error al procesar el grupo"
        )
    }
}
