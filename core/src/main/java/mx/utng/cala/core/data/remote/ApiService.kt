package mx.utng.cala.core.data.remote

import mx.utng.cala.core.data.dto.request.*
import mx.utng.cala.core.data.dto.response.*
import retrofit2.Response
import retrofit2.http.*

/** Define los endpoints REST compartidos por las aplicaciones de Ruta Libre. */
interface ApiService {

    /** Autentica a un usuario y devuelve sus datos de sesion. */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    /** Registra una cuenta nueva en el backend. */
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<Unit>

    /** Obtiene el perfil de un usuario por su identificador. */
    @GET("usuarios/{id}")
    suspend fun getUsuario(@Path("id") idUsuario: Int): Response<UsuarioResponse>

    /** Actualiza los datos editables del perfil de un usuario. */
    @PUT("usuarios/{id}")
    suspend fun actualizarUsuario(
        @Path("id") idUsuario: Int,
        @Body peticion: ActualizarUsuarioPeticion
    ): Response<Unit>

    /** Guarda el peso actual del usuario. */
    @PUT("usuarios/{id}/peso")
    suspend fun actualizarPeso(
        @Path("id") idUsuario: Int,
        @Body peticion: ActualizarPesoPeticion
    ): Response<Unit>

    /** Inicia un entrenamiento para el usuario indicado. */
    @POST("entrenamientos/iniciar")
    suspend fun iniciarEntrenamiento(@Body request: IniciarEntrenamientoRequest): Response<EntrenamientoResponse>

    /** Finaliza un entrenamiento y guarda sus metricas y coordenadas. */
    @PUT("entrenamientos/finalizar")
    suspend fun finalizarEntrenamiento(@Body request: FinalizarEntrenamientoRequest): Response<EntrenamientoResponse>

    /** Consulta el entrenamiento que permanece activo. */
    @GET("entrenamientos/activo/{idUsuario}")
    suspend fun getEntrenamientoActivo(@Path("idUsuario") idUsuario: Int): Response<EntrenamientoActivoResponse>

    /** Obtiene el historial de entrenamientos del usuario. */
    @GET("entrenamientos/usuario/{idUsuario}")
    suspend fun getHistorialEntrenamientos(@Path("idUsuario") idUsuario: Int): Response<List<EntrenamientoResponse>>

    /** Obtiene los totales y el detalle diario de la semana. */
    @GET("entrenamientos/semana/{idUsuario}")
    suspend fun getDashboardSemanal(@Path("idUsuario") idUsuario: Int): Response<DashboardSemanalResponse>

    /** Obtiene los totales y el detalle semanal del mes. */
    @GET("entrenamientos/mes/{idUsuario}")
    suspend fun obtenerDashboardMensual(@Path("idUsuario") idUsuario: Int): Response<RespuestaDashboardMensual>

    /** Compara el rendimiento semanal con el periodo anterior. */
    @GET("entrenamientos/comparacion/{idUsuario}")
    suspend fun getComparacionRendimiento(@Path("idUsuario") idUsuario: Int): Response<ComparacionRendimientoResponse>

    /** Compara el rendimiento mensual con el periodo anterior. */
    @GET("entrenamientos/comparacion-mes/{idUsuario}")
    suspend fun obtenerComparacionMensual(@Path("idUsuario") idUsuario: Int): Response<ComparacionRendimientoResponse>

    /** Actualiza las coordenadas de una ruta existente. */
    @POST("rutas/actualizar")
    suspend fun actualizarRuta(@Body request: ActualizarRutaRequest): Response<RutaResponse>

    /** Recupera una ruta con todas sus coordenadas. */
    @GET("rutas/{id}")
    suspend fun getRuta(@Path("id") idRuta: Int): Response<RutaResponse>

    /** Crea una meta personal de rendimiento. */
    @POST("metas")
    suspend fun crearMeta(@Body request: CrearMetaRequest): Response<MetaResponse>

    /** Lista las metas asociadas al usuario. */
    @GET("metas/usuario/{idUsuario}")
    suspend fun getMetas(@Path("idUsuario") idUsuario: Int): Response<List<MetaResponse>>

    /** Actualiza el valor objetivo de una meta. */
    @PUT("metas/{idMetas}")
    suspend fun actualizarMeta(@Path("idMetas") idMetas: Int, @Body request: ActualizarMetaRequest): Response<MetaResponse>

    /** Elimina una meta del usuario. */
    @DELETE("metas/{idMetas}")
    suspend fun eliminarMeta(@Path("idMetas") idMetas: Int): Response<Unit>

    /** Crea un grupo de entrenamiento. */
    @POST("grupos")
    suspend fun crearGrupo(@Body request: CrearGrupoRequest): Response<GrupoResponse>

    /** Agrega un usuario a un grupo mediante su codigo. */
    @POST("grupos/unirse")
    suspend fun unirseGrupo(@Body request: UnirseGrupoRequest): Response<Unit>

    /** Lista los grupos a los que pertenece un usuario. */
    @GET("grupos/usuario/{idUsuario}")
    suspend fun getGruposUsuario(@Path("idUsuario") idUsuario: Int): Response<List<GrupoResponse>>

    /** Obtiene los miembros y sus metricas del grupo. */
    @GET("grupos/{idGrupo}/miembros")
    suspend fun getMiembrosGrupo(@Path("idGrupo") idGrupo: Int): Response<List<MiembroGrupoResponse>>

    /** Obtiene las estadisticas semanales de un miembro del grupo. */
    @GET("grupos/{idGrupo}/miembros/{idUsuario}/estadisticas")
    suspend fun getEstadisticasMiembroGrupo(
        @Path("idGrupo") idGrupo: Int,
        @Path("idUsuario") idUsuario: Int
    ): Response<DashboardSemanalResponse>

    /** Obtiene el ranking semanal del grupo. */
    @GET("grupos/{idGrupo}/ranking")
    suspend fun getRankingGrupo(@Path("idGrupo") idGrupo: Int): Response<RankingResponse>

    /** Retira a un usuario de un grupo. */
    @DELETE("grupos/{idGrupo}/miembros/{idUsuario}")
    suspend fun salirDeGrupo(
        @Path("idGrupo") idGrupo: Int,
        @Path("idUsuario") idUsuario: Int
    ): Response<Unit>

    /** Elimina un grupo cuando la operacion la autoriza su creador. */
    @DELETE("grupos/{idGrupo}")
    suspend fun eliminarGrupo(
        @Path("idGrupo") idGrupo: Int,
        @Query("idUsuario") idUsuario: Int
    ): Response<Unit>

    /** Lista las notificaciones del usuario. */
    @GET("notificaciones/usuario/{idUsuario}")
    suspend fun getNotificaciones(@Path("idUsuario") idUsuario: Int): Response<List<NotificacionResponse>>

    /** Marca una notificacion como leida en la aplicacion movil. */
    @PUT("notificaciones/{id}/leer-movil")
    suspend fun marcarLeidaMovil(@Path("id") idNotificacion: Int): Response<Unit>

    /** Marca una notificacion como leida en el smartwatch. */
    @PUT("notificaciones/{id}/leer-wear")
    suspend fun marcarLeidaSmartwatch(@Path("id") idNotificacion: Int): Response<Unit>

    /** Solicita un codigo temporal para vincular un dispositivo. */
    @POST("dispositivos/solicitar-vinculacion")
    suspend fun solicitarVinculacion(
        @Body request: SolicitarVinculacionRequest
    ): Response<SolicitudVinculacionResponse>

    /** Consulta si un codigo de vinculacion ya fue autorizado. */
    @POST("dispositivos/estado-vinculacion")
    suspend fun consultarEstadoVinculacion(
        @Body request: EstadoVinculacionRequest
    ): Response<EstadoVinculacionResponse>

    /** Vincula un dispositivo usando el token del usuario. */
    @POST("dispositivos/vincular")
    suspend fun vincularDispositivo(
        @Header("Authorization") authorization: String,
        @Body request: VincularDispositivoRequest
    ): Response<DispositivoVinculadoResponse>

    /** Registra un dispositivo Wear OS para el usuario autenticado. */
    @POST("dispositivos/vincular-wear")
    suspend fun vincularWear(
        @Header("Authorization") authorization: String,
        @Body request: VincularWearRequest
    ): Response<WearVinculadoResponse>

    /** Lista los dispositivos asociados a la cuenta. */
    @GET("dispositivos")
    suspend fun listarDispositivos(
        @Header("Authorization") authorization: String
    ): Response<List<DispositivoResponse>>

    /** Desvincula un dispositivo especifico de la cuenta. */
    @DELETE("dispositivos/{idDispositivo}")
    suspend fun desvincularDispositivo(
        @Header("Authorization") authorization: String,
        @Path("idDispositivo") idDispositivo: String
    ): Response<Unit>

    /** Cierra la sesion del dispositivo actual. */
    @DELETE("dispositivos/sesion/actual")
    suspend fun cerrarSesionDispositivo(
        @Header("Authorization") authorization: String
    ): Response<Unit>

    /** Comprueba que la sesion del dispositivo siga siendo valida. */
    @GET("dispositivos/sesion/actual")
    suspend fun validarSesionDispositivo(
        @Header("Authorization") authorization: String
    ): Response<Unit>

    /** Cierra todas las sesiones de dispositivos del usuario. */
    @DELETE("dispositivos/sesion/todos")
    suspend fun cerrarTodasLasSesiones(
        @Header("Authorization") authorization: String
    ): Response<Unit>
}
