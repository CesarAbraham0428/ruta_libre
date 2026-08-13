package mx.utng.cala.core.data.remote

import java.util.concurrent.TimeUnit
import mx.utng.cala.core.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** Provee el cliente Retrofit configurado para consumir el backend. */
object RetrofitClient {

    private val httpClient = OkHttpClient.Builder()
        // Render puede tardar varios segundos en reactivar una instancia inactiva.
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .callTimeout(90, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .addInterceptor(TransientHttpRetryInterceptor())
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /** Servicio REST listo para ser usado por los repositorios. */
    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
