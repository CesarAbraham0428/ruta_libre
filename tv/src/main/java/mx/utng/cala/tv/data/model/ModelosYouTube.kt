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
data class RespuestaBusquedaYouTube(
    @SerializedName("items") val elementos: List<ElementoVideoYouTube>
)

data class ElementoVideoYouTube(
    @SerializedName("id") val identificador: IdVideoYouTube,
    @SerializedName("snippet") val info: InfoVideoYouTube
)

data class IdVideoYouTube(
    @SerializedName("videoId") val idVideo: String?
)

data class InfoVideoYouTube(
    @SerializedName("title") val titulo: String,
    @SerializedName("description") val descripcion: String,
    @SerializedName("channelTitle") val canal: String,
    @SerializedName("publishedAt") val fechaPublicacion: String,
    @SerializedName("thumbnails") val miniaturas: MiniaturasYouTube
)

data class MiniaturasYouTube(
    @SerializedName("medium") val mediana: DetalleMiniaturaYouTube?,
    @SerializedName("high") val alta: DetalleMiniaturaYouTube?
)

data class DetalleMiniaturaYouTube(
    @SerializedName("url") val url: String
)

data class RespuestaDetallesVideosYouTube(
    @SerializedName("items") val elementos: List<DetalleVideoYouTube>
)

data class DetalleVideoYouTube(
    @SerializedName("id") val id: String,
    @SerializedName("contentDetails") val contenido: ContenidoVideoYouTube?,
    @SerializedName("statistics") val estadisticas: EstadisticasVideoYouTube?
)

data class ContenidoVideoYouTube(
    @SerializedName("duration") val duracionIso: String?
)

data class EstadisticasVideoYouTube(
    @SerializedName("viewCount") val cantidadVistas: String?
)
