package mx.utng.cala.tv.data.remote

import mx.utng.cala.tv.data.model.RespuestaBusquedaYouTube
import mx.utng.cala.tv.data.model.RespuestaDetallesVideosYouTube
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/** Define las consultas Retrofit realizadas contra YouTube Data API. */
interface ServicioApiYouTube {

    /** Busca videos de running con filtros aptos para reproduccion. */
    @GET("search")
    suspend fun buscarVideos(
        @Query("part") parte: String = "snippet",
        @Query("q") consulta: String,
        @Query("type") tipo: String = "video",
        @Query("maxResults") maxResultados: Int = 25,
        @Query("order") orden: String = "relevance",
        @Query("safeSearch") busquedaSegura: String = "strict",
        @Query("relevanceLanguage") idioma: String = "es",
        @Query("regionCode") region: String = "MX",
        @Query("videoCategoryId") categoriaVideo: String = "17",
        @Query("videoEmbeddable") reproducibleInsertado: String = "true",
        @Query("videoSyndicated") reproducibleFueraDeYouTube: String = "true",
        @Query("key") clave: String
    ): Response<RespuestaBusquedaYouTube>

    /** Obtiene duracion y cantidad de vistas de los videos encontrados. */
    @GET("videos")
    suspend fun obtenerDetallesVideos(
        @Query("part") parte: String = "contentDetails,statistics",
        @Query("id") ids: String,
        @Query("key") clave: String
    ): Response<RespuestaDetallesVideosYouTube>
}
