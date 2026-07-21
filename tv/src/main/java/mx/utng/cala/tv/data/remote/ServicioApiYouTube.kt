package mx.utng.cala.tv.data.remote

import mx.utng.cala.tv.data.model.RespuestaBusquedaYouTube
import mx.utng.cala.tv.data.model.RespuestaDetallesVideosYouTube
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ServicioApiYouTube {

    @GET("search")
    suspend fun buscarVideos(
        @Query("part") parte: String = "snippet",
        @Query("q") consulta: String,
        @Query("type") tipo: String = "video",
        @Query("maxResults") maxResultados: Int = 10,
        @Query("key") clave: String
    ): Response<RespuestaBusquedaYouTube>

    @GET("videos")
    suspend fun obtenerDetallesVideos(
        @Query("part") parte: String = "contentDetails,statistics",
        @Query("id") ids: String,
        @Query("key") clave: String
    ): Response<RespuestaDetallesVideosYouTube>
}
