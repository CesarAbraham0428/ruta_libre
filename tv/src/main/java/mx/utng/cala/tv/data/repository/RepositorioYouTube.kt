package mx.utng.cala.tv.data.repository

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
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
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(ConfiguracionYouTube.URL_BASE)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ServicioApiYouTube::class.java)
    }

    private val listaVideosSimulados = listOf(
        VideoRutaLibre(
            id = "LGP74Qd-U9M",
            titulo = "Cómo empezar a correr desde cero — Guía para principiantes",
            descripcion = "Los mejores consejos para iniciarte en el running de forma segura y evitar lesiones comunes.",
            urlMiniatura = "https://i.ytimg.com/vi/LGP74Qd-U9M/hqdefault.jpg",
            autor = "Ruta Activa",
            duracion = "12:30",
            fechaPublicacion = "10 Ene 2026",
            vistas = "150K vistas",
            categoria = "Videos"
        ),
        VideoRutaLibre(
            id = "8eXvO8v8b6U",
            titulo = "Técnica de carrera: Ejercicios clave para correr mejor",
            descripcion = "Aprende a mejorar tu postura, zancada y respiración con estos ejercicios prácticos de técnica.",
            urlMiniatura = "https://i.ytimg.com/vi/8eXvO8v8b6U/hqdefault.jpg",
            autor = "Entrena Runner",
            duracion = "8:45",
            fechaPublicacion = "15 Feb 2026",
            vistas = "95K vistas",
            categoria = "Consejos"
        ),
        VideoRutaLibre(
            id = "e7h7G12_tP4",
            titulo = "Cómo correr tu primer 5K: Plan de entrenamiento",
            descripcion = "Una guía completa paso a paso con el plan ideal de entrenamiento para cruzar tu primera meta de 5K.",
            urlMiniatura = "https://i.ytimg.com/vi/e7h7G12_tP4/hqdefault.jpg",
            autor = "Maratón Tips",
            duracion = "10:15",
            fechaPublicacion = "20 Mar 2026",
            vistas = "220K vistas",
            categoria = "Carreras"
        ),
        VideoRutaLibre(
            id = "5K-2F4e0F8Y",
            titulo = "5 Consejos para correr sin cansarte rápido",
            descripcion = "Aprende a dosificar tu energía, respirar correctamente y mantener un ritmo constante de carrera.",
            urlMiniatura = "https://i.ytimg.com/vi/5K-2F4e0F8Y/hqdefault.jpg",
            autor = "Salud & Running",
            duracion = "6:20",
            fechaPublicacion = "05 Abr 2026",
            vistas = "480K vistas",
            categoria = "Tips"
        ),
        VideoRutaLibre(
            id = "wK-A1b8ZpC4",
            titulo = "Estiramientos esenciales antes y después de correr",
            descripcion = "Rutina completa de estiramientos dinámicos y estáticos para preparar tus músculos y mejorar la recuperación.",
            urlMiniatura = "https://i.ytimg.com/vi/wK-A1b8ZpC4/hqdefault.jpg",
            autor = "Entrena Runner",
            duracion = "9:15",
            fechaPublicacion = "12 Mayo 2026",
            vistas = "112K vistas",
            categoria = "Consejos"
        ),
        VideoRutaLibre(
            id = "yXz2Wq1v8U0",
            titulo = "Cómo prepararte para el día de la carrera",
            descripcion = "Qué comer, qué llevar en tu equipamiento y cómo mentalizarte el día anterior a tu gran evento deportivo.",
            urlMiniatura = "https://i.ytimg.com/vi/yXz2Wq1v8U0/hqdefault.jpg",
            autor = "Maratón Tips",
            duracion = "11:40",
            fechaPublicacion = "18 Jun 2026",
            vistas = "78K vistas",
            categoria = "Carreras"
        )
    )

    private fun obtenerVideosSimulados(consulta: String, categoria: String): List<VideoRutaLibre> {
        val palabrasConsulta = consulta.lowercase().trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
        val categoriaLimpia = if (categoria == "Videos") "" else categoria

        return listaVideosSimulados.filter { video ->
            val coincideCategoria = categoriaLimpia.isBlank() || video.categoria.equals(categoriaLimpia, ignoreCase = true)
            val coincideConsulta = palabrasConsulta.isEmpty() || palabrasConsulta.all { palabra ->
                video.titulo.lowercase().contains(palabra) || 
                video.descripcion.lowercase().contains(palabra) ||
                video.autor.lowercase().contains(palabra) ||
                video.categoria.lowercase().contains(palabra)
            }
            coincideCategoria && coincideConsulta
        }
    }

    /** Consulta la API de YouTube. Utiliza fallback automático a datos simulados ante fallas. */
    suspend fun buscarVideos(consulta: String, categoria: String): List<VideoRutaLibre> {
        val claveApi = ConfiguracionYouTube.CLAVE_API.trim()
        android.util.Log.d("RepositorioYouTube", "Iniciando búsqueda de videos: consulta='$consulta', categoria='$categoria'")

        if (claveApi.isBlank()) {
            android.util.Log.w("RepositorioYouTube", "La clave API de YouTube está vacía. Cargando videos simulados de fallback.")
            return obtenerVideosSimulados(consulta, categoria)
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
        } catch (error: Throwable) {
            android.util.Log.e("RepositorioYouTube", "Error al consultar la API de YouTube: ${error.message}. Usando videos simulados de fallback.", error)
            return obtenerVideosSimulados(consulta, categoria)
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
