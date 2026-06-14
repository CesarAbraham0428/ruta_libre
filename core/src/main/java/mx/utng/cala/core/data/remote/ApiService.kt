package mx.utng.cala.core.data.remote

import mx.utng.cala.core.data.dto.request.*
import mx.utng.cala.core.data.dto.response.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<Unit>

    @GET("usuarios/{id}")
    suspend fun getUsuario(@Path("id") idUsuario: Int): Response<UsuarioResponse>

    @POST("entrenamientos/iniciar")
    suspend fun iniciarEntrenamiento(@Body request: IniciarEntrenamientoRequest): Response<EntrenamientoResponse>

    @PUT("entrenamientos/finalizar")
    suspend fun finalizarEntrenamiento(@Body request: FinalizarEntrenamientoRequest): Response<EntrenamientoResponse>

    @GET("entrenamientos/activo/{idUsuario}")
    suspend fun getEntrenamientoActivo(@Path("idUsuario") idUsuario: Int): Response<EntrenamientoActivoResponse>

    @GET("entrenamientos/usuario/{idUsuario}")
    suspend fun getHistorialEntrenamientos(@Path("idUsuario") idUsuario: Int): Response<List<EntrenamientoResponse>>

    @GET("entrenamientos/semana/{idUsuario}")
    suspend fun getDashboardSemanal(@Path("idUsuario") idUsuario: Int): Response<DashboardSemanalResponse>

    @GET("entrenamientos/comparacion/{idUsuario}")
    suspend fun getComparacionRendimiento(@Path("idUsuario") idUsuario: Int): Response<ComparacionRendimientoResponse>

    @POST("rutas/actualizar")
    suspend fun actualizarRuta(@Body request: ActualizarRutaRequest): Response<RutaResponse>

    @GET("rutas/{id}")
    suspend fun getRuta(@Path("id") idRuta: Int): Response<RutaResponse>

    @POST("metas")
    suspend fun crearMeta(@Body request: CrearMetaRequest): Response<MetaResponse>

    @GET("metas/usuario/{idUsuario}")
    suspend fun getMetas(@Path("idUsuario") idUsuario: Int): Response<List<MetaResponse>>

    @POST("grupos")
    suspend fun crearGrupo(@Body request: CrearGrupoRequest): Response<GrupoResponse>

    @POST("grupos/unirse")
    suspend fun unirseGrupo(@Body request: UnirseGrupoRequest): Response<Unit>

    @GET("grupos/usuario/{idUsuario}")
    suspend fun getGruposUsuario(@Path("idUsuario") idUsuario: Int): Response<List<GrupoResponse>>

    @GET("grupos/{idGrupo}/miembros")
    suspend fun getMiembrosGrupo(@Path("idGrupo") idGrupo: Int): Response<List<MiembroGrupoResponse>>

    @GET("grupos/{idGrupo}/ranking")
    suspend fun getRankingGrupo(@Path("idGrupo") idGrupo: Int): Response<RankingResponse>

    @GET("notificaciones/usuario/{idUsuario}")
    suspend fun getNotificaciones(@Path("idUsuario") idUsuario: Int): Response<List<NotificacionResponse>>

    @PUT("notificaciones/{id}/leer-movil")
    suspend fun marcarLeidaMovil(@Path("id") idNotificacion: Int): Response<Unit>

    @PUT("notificaciones/{id}/leer-wear")
    suspend fun marcarLeidaSmartwatch(@Path("id") idNotificacion: Int): Response<Unit>
}
