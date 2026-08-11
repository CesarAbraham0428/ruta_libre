package mx.utng.cala.tv.data.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo interno de video simplificado para el uso en las pantallas de la aplicación.
 */
data class VideoRutaLibre(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val urlMiniatura: String,
    val autor: String,
    val duracion: String?,
    val fechaPublicacion: String,
    val vistas: String?,
    val categoria: String
)

/**
 * Modelos de datos para mapear la respuesta JSON de la API de YouTube.
 */
/** Respuesta de busqueda de videos recibida desde YouTube. */
data class RespuestaBusquedaYouTube(
    @SerializedName("items") val elementos: List<ElementoVideoYouTube>
)

/** Elemento individual de una respuesta de busqueda. */
data class ElementoVideoYouTube(
    @SerializedName("id") val identificador: IdVideoYouTube,
    @SerializedName("snippet") val info: InfoVideoYouTube
)

/** Identificador de video incluido por la API de YouTube. */
data class IdVideoYouTube(
    @SerializedName("videoId") val idVideo: String?
)

/** Metadatos visibles de un video de YouTube. */
data class InfoVideoYouTube(
    @SerializedName("title") val titulo: String,
    @SerializedName("description") val descripcion: String,
    @SerializedName("channelTitle") val canal: String,
    @SerializedName("publishedAt") val fechaPublicacion: String,
    @SerializedName("thumbnails") val miniaturas: MiniaturasYouTube
)

/** URLs de las miniaturas disponibles para un video. */
data class MiniaturasYouTube(
    @SerializedName("medium") val mediana: DetalleMiniaturaYouTube?,
    @SerializedName("high") val alta: DetalleMiniaturaYouTube?
)

/** Datos de una miniatura de YouTube. */
data class DetalleMiniaturaYouTube(
    @SerializedName("url") val url: String
)

/** Respuesta con detalles adicionales de varios videos. */
data class RespuestaDetallesVideosYouTube(
    @SerializedName("items") val elementos: List<DetalleVideoYouTube>
)

/** Duracion y estadisticas asociadas a un video. */
data class DetalleVideoYouTube(
    @SerializedName("id") val id: String,
    @SerializedName("contentDetails") val contenido: ContenidoVideoYouTube?,
    @SerializedName("statistics") val estadisticas: EstadisticasVideoYouTube?
)

/** Detalles de reproduccion de un video. */
data class ContenidoVideoYouTube(
    @SerializedName("duration") val duracionIso: String?
)

/** Estadisticas publicas de un video. */
data class EstadisticasVideoYouTube(
    @SerializedName("viewCount") val cantidadVistas: String?
)
