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

/** Estado observable de filtros, videos, carga y seleccion actual. */
data class EstadoUiVideos(
    val estaCargando: Boolean = false,
    val listaVideos: List<VideoRutaLibre> = emptyList(),
    val filtroActivo: String = "Videos",
    val subfiltroActivo: String = "Todos",
    val terminoBusqueda: String = "",
    val videoSeleccionado: VideoRutaLibre? = null,
    val error: String? = null
)

/** Gestiona busqueda, filtros y seleccion de videos para la TV. */
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
            } catch (error: Throwable) {
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

    /** Cambia la categoria principal y recarga los videos. */
    fun cambiarFiltro(nuevoFiltro: String) {
        _estadoUi.update {
            it.copy(filtroActivo = nuevoFiltro, subfiltroActivo = "Todos", videoSeleccionado = null)
        }
        cargarVideos()
    }

    /** Cambia el nivel de contenido y recarga los resultados. */
    fun cambiarSubfiltro(nuevoSubfiltro: String) {
        _estadoUi.update { it.copy(subfiltroActivo = nuevoSubfiltro, videoSeleccionado = null) }
        cargarVideos()
    }

    /** Actualiza el texto de busqueda aplicando debounce en la carga. */
    fun buscarPorTexto(texto: String) {
        _estadoUi.update { it.copy(terminoBusqueda = texto, videoSeleccionado = null) }
        cargarVideos(aplicarEspera = true)
    }

    /** Define el video que se mostrara en el reproductor. */
    fun seleccionarVideo(video: VideoRutaLibre?) {
        _estadoUi.update { it.copy(videoSeleccionado = video) }
    }

    /** Cancela la carga pendiente cuando se destruye el ViewModel. */
    override fun onCleared() {
        trabajoCarga?.cancel()
        super.onCleared()
    }

    /** Construye la consulta combinando texto, categoria y subfiltro. */
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
