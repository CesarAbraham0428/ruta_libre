package mx.utng.cala.tv.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.utng.cala.tv.data.model.VideoRutaLibre
import mx.utng.cala.tv.data.repository.RepositorioYouTube

data class EstadoUiVideos(
    val estaCargando: Boolean = false,
    val listaVideos: List<VideoRutaLibre> = emptyList(),
    val filtroActivo: String = "Videos",
    val subfiltroActivo: String = "Todos",
    val terminoBusqueda: String = "",
    val videoSeleccionado: VideoRutaLibre? = null,
    val error: String? = null
)

class ViewModelVideos : ViewModel() {

    private val repositorio = RepositorioYouTube()
    private val _estadoUi = MutableStateFlow(EstadoUiVideos())
    val estadoUi: StateFlow<EstadoUiVideos> = _estadoUi.asStateFlow()
    private var trabajoCarga: Job? = null

    init {
        cargarVideos()
    }

    /** Cancela la petición anterior; el buscador espera 400 ms antes de consultar la API. */
    fun cargarVideos(aplicarEspera: Boolean = false) {
        trabajoCarga?.cancel()
        val consultaSolicitada = construirConsulta()
        val filtroSolicitado = _estadoUi.value.filtroActivo
        _estadoUi.update { it.copy(estaCargando = true, error = null) }

        trabajoCarga = viewModelScope.launch {
            try {
                if (aplicarEspera) delay(400)
                val videos = repositorio.buscarVideos(consultaSolicitada, filtroSolicitado)
                _estadoUi.update { it.copy(estaCargando = false, listaVideos = videos) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _estadoUi.update {
                    it.copy(
                        estaCargando = false,
                        error = error.message ?: "No se pudieron cargar los videos.",
                        listaVideos = emptyList()
                    )
                }
            }
        }
    }

    fun cambiarFiltro(nuevoFiltro: String) {
        _estadoUi.update {
            it.copy(filtroActivo = nuevoFiltro, subfiltroActivo = "Todos", videoSeleccionado = null)
        }
        cargarVideos()
    }

    fun cambiarSubfiltro(nuevoSubfiltro: String) {
        _estadoUi.update { it.copy(subfiltroActivo = nuevoSubfiltro, videoSeleccionado = null) }
        cargarVideos()
    }

    fun buscarPorTexto(texto: String) {
        _estadoUi.update { it.copy(terminoBusqueda = texto, videoSeleccionado = null) }
        cargarVideos(aplicarEspera = true)
    }

    fun seleccionarVideo(video: VideoRutaLibre?) {
        _estadoUi.update { it.copy(videoSeleccionado = video) }
    }

    override fun onCleared() {
        trabajoCarga?.cancel()
        super.onCleared()
    }

    private fun construirConsulta(): String {
        val estado = _estadoUi.value
        return when {
            estado.terminoBusqueda.isNotBlank() -> estado.terminoBusqueda
            estado.subfiltroActivo != "Todos" -> "${estado.filtroActivo} ${estado.subfiltroActivo}"
            estado.filtroActivo != "Videos" -> estado.filtroActivo
            else -> ""
        }
    }
}
