package mx.utng.cala.tv.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.utng.cala.core.data.dto.response.DashboardSemanalResponse
import mx.utng.cala.core.data.dto.response.GrupoResponse
import mx.utng.cala.core.data.dto.response.MiembroGrupoResponse
import mx.utng.cala.core.data.repository.GrupoRepository

data class EstadoUiGrupoTv(
    val listaGrupos: List<GrupoResponse> = emptyList(),
    val listaMiembros: List<MiembroGrupoResponse> = emptyList(),
    val listaRanking: List<MiembroGrupoResponse> = emptyList(),
    val estadisticasMiembro: Map<Int, DashboardSemanalResponse> = emptyMap(),
    val miembroEstadisticasCargandoId: Int? = null,
    val mensajeErrorEstadisticas: String? = null,
    val estaCargando: Boolean = false,
    val mensajeError: String? = null
)

class GrupoTvViewModel : ViewModel() {

    private val repositorio = GrupoRepository()
    private val _estadoUi = MutableStateFlow(EstadoUiGrupoTv())
    val estadoUi: StateFlow<EstadoUiGrupoTv> = _estadoUi

    fun cargarGruposDeUsuario(idUsuario: Int) {
        viewModelScope.launch {
            _estadoUi.value = _estadoUi.value.copy(estaCargando = true, mensajeError = null)
            repositorio.getGrupos(idUsuario).fold(
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
            repositorio.crearGrupo(nombre, descripcion, idCreador).fold(
                onSuccess = { cargarGruposDeUsuario(idCreador) },
                onFailure = { error -> mostrarError(error) }
            )
        }
    }

    fun unirseAGrupoConCodigo(idUsuario: Int, codigo: String) {
        viewModelScope.launch {
            _estadoUi.value = _estadoUi.value.copy(estaCargando = true, mensajeError = null)
            repositorio.unirseGrupo(idUsuario, codigo).fold(
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
                listaRanking = emptyList(),
                estadisticasMiembro = emptyMap(),
                miembroEstadisticasCargandoId = null,
                mensajeErrorEstadisticas = null
            )

            val miembrosDiferidos = async { repositorio.getMiembros(idGrupo) }
            val rankingDiferido = async { repositorio.getRanking(idGrupo) }

            val resultadoMiembros = miembrosDiferidos.await()
            val resultadoRanking = rankingDiferido.await()

            val miembros = resultadoMiembros.getOrNull()
            val ranking = resultadoRanking.getOrNull()
            val error = listOf(resultadoMiembros, resultadoRanking).firstNotNullOfOrNull { it.exceptionOrNull() }

            _estadoUi.value = _estadoUi.value.copy(
                listaMiembros = miembros ?: emptyList(),
                listaRanking = ranking?.miembros ?: emptyList(),
                estaCargando = false,
                mensajeError = error?.message
            )
        }
    }

    fun cargarEstadisticasMiembro(idUsuario: Int) {
        if (_estadoUi.value.estadisticasMiembro.containsKey(idUsuario)) return

        viewModelScope.launch {
            _estadoUi.value = _estadoUi.value.copy(
                miembroEstadisticasCargandoId = idUsuario,
                mensajeErrorEstadisticas = null
            )

            repositorio.getEstadisticasMiembro(idUsuario).fold(
                onSuccess = { estadisticas ->
                    val estadoActual = _estadoUi.value
                    _estadoUi.value = _estadoUi.value.copy(
                        estadisticasMiembro = estadoActual.estadisticasMiembro + (idUsuario to estadisticas),
                        miembroEstadisticasCargandoId = estadoActual.miembroEstadisticasCargandoId
                            .takeUnless { it == idUsuario },
                        mensajeErrorEstadisticas = null
                    )
                },
                onFailure = { error ->
                    val estadoActual = _estadoUi.value
                    _estadoUi.value = _estadoUi.value.copy(
                        miembroEstadisticasCargandoId = estadoActual.miembroEstadisticasCargandoId
                            .takeUnless { it == idUsuario },
                        mensajeErrorEstadisticas = error.message ?: "No se pudieron cargar las estadisticas del miembro"
                    )
                }
            )
        }
    }

    fun salirDeGrupo(idUsuario: Int, idGrupo: Int, alTenerExito: () -> Unit = {}) {
        viewModelScope.launch {
            _estadoUi.value = _estadoUi.value.copy(estaCargando = true, mensajeError = null)
            repositorio.salirDeGrupo(idUsuario, idGrupo).fold(
                onSuccess = {
                    _estadoUi.value = _estadoUi.value.copy(
                        listaGrupos = _estadoUi.value.listaGrupos.filter { it.idGrupo != idGrupo },
                        estaCargando = false
                    )
                    alTenerExito()
                },
                onFailure = { error -> mostrarError(error) }
            )
        }
    }

    fun eliminarGrupo(idUsuario: Int, idGrupo: Int, alTenerExito: () -> Unit = {}) {
        viewModelScope.launch {
            _estadoUi.value = _estadoUi.value.copy(estaCargando = true, mensajeError = null)
            repositorio.eliminarGrupo(idGrupo, idUsuario).fold(
                onSuccess = {
                    _estadoUi.value = _estadoUi.value.copy(
                        listaGrupos = _estadoUi.value.listaGrupos.filter { it.idGrupo != idGrupo },
                        estaCargando = false
                    )
                    alTenerExito()
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
