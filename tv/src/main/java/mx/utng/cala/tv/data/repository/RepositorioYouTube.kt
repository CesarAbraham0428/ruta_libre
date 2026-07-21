package mx.utng.cala.tv.data.repository

import kotlinx.coroutines.CancellationException
import mx.utng.cala.tv.data.model.DetalleVideoYouTube
import mx.utng.cala.tv.data.model.VideoRutaLibre
import mx.utng.cala.tv.data.remote.ConfiguracionYouTube
import mx.utng.cala.tv.data.remote.ServicioApiYouTube
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Locale

class ErrorApiYouTube(message: String) : Exception(message)

class RepositorioYouTube {

    private val servicioApi: ServicioApiYouTube by lazy {
        Retrofit.Builder()
            .baseUrl(ConfiguracionYouTube.URL_BASE)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ServicioApiYouTube::class.java)
    }

    /** Consulta la API de YouTube. Nunca reemplaza errores reales con datos simulados. */
    suspend fun buscarVideos(consulta: String, categoria: String): List<VideoRutaLibre> {
        val claveApi = ConfiguracionYouTube.CLAVE_API.trim()
        if (claveApi.isBlank()) {
            throw ErrorApiYouTube("No se configuró la clave de la API de YouTube.")
        }

        try {
            val termino = listOf("running", consulta.trim())
                .filter { it.isNotBlank() }
                .joinToString(" ")
            val respuestaBusqueda = servicioApi.buscarVideos(consulta = termino, clave = claveApi)
            validarRespuesta(respuestaBusqueda, "buscar videos")
            val elementos = respuestaBusqueda.body()?.elementos.orEmpty()
            val ids = elementos.mapNotNull { it.identificador.idVideo }
            if (ids.isEmpty()) return emptyList()

            val respuestaDetalles = servicioApi.obtenerDetallesVideos(
                ids = ids.joinToString(","),
                clave = claveApi
            )
            validarRespuesta(respuestaDetalles, "obtener los detalles de los videos")
            val detallesPorId = respuestaDetalles.body()?.elementos.orEmpty().associateBy(DetalleVideoYouTube::id)

            return elementos.mapNotNull { elemento ->
                val idVideo = elemento.identificador.idVideo ?: return@mapNotNull null
                val detalle = detallesPorId[idVideo]
                VideoRutaLibre(
                    id = idVideo,
                    titulo = elemento.info.titulo,
                    descripcion = elemento.info.descripcion,
                    urlMiniatura = elemento.info.miniaturas.alta?.url
                        ?: elemento.info.miniaturas.mediana?.url
                        ?: "https://i.ytimg.com/vi/$idVideo/hqdefault.jpg",
                    autor = elemento.info.canal,
                    duracion = detalle?.contenido?.duracionIso?.let(::formatearDuracion),
                    fechaPublicacion = formatearFecha(elemento.info.fechaPublicacion),
                    vistas = detalle?.estadisticas?.cantidadVistas?.let(::formatearVistas),
                    categoria = categoria
                )
            }
        } catch (error: CancellationException) {
            throw error
        }
    }

    private fun validarRespuesta(respuesta: Response<*>, accion: String) {
        if (respuesta.isSuccessful) return
        val causa = when (respuesta.code()) {
            400 -> "La solicitud enviada a YouTube no es válida."
            403 -> "YouTube rechazó la solicitud. Revisa que la clave permita YouTube Data API v3, sus restricciones y la cuota disponible."
            404 -> "No se encontró el recurso solicitado en YouTube."
            else -> "YouTube respondió con el código HTTP ${respuesta.code()}."
        }
        throw ErrorApiYouTube("No se pudo $accion. $causa")
    }

    private fun formatearFecha(fechaIso: String): String = try {
        val entrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        val salida = SimpleDateFormat("dd MMM yyyy", Locale("es", "MX"))
        entrada.parse(fechaIso)?.let(salida::format) ?: fechaIso
    } catch (_: Exception) {
        fechaIso
    }

    private fun formatearDuracion(duracionIso: String): String {
        val coincidencia = Regex("PT(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?").matchEntire(duracionIso)
            ?: return duracionIso
        val horas = coincidencia.groupValues[1].toIntOrNull() ?: 0
        val minutos = coincidencia.groupValues[2].toIntOrNull() ?: 0
        val segundos = coincidencia.groupValues[3].toIntOrNull() ?: 0
        return if (horas > 0) "%d:%02d:%02d".format(horas, minutos, segundos)
        else "%d:%02d".format(minutos, segundos)
    }

    private fun formatearVistas(vistas: String): String {
        val cantidad = vistas.toLongOrNull() ?: return "$vistas vistas"
        val texto = when {
            cantidad >= 1_000_000 -> "%.1f M".format(Locale.US, cantidad / 1_000_000.0)
            cantidad >= 1_000 -> "%.1f K".format(Locale.US, cantidad / 1_000.0)
            else -> cantidad.toString()
        }.replace(".0", "")
        return "$texto vistas"
    }
}
