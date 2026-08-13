package mx.utng.cala.core.data.remote

import java.io.IOException
import java.util.concurrent.TimeUnit
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Reintenta de forma acotada las consultas de solo lectura cuando Render o la red
 * responden con un fallo temporal. Las operaciones de escritura no se repiten para
 * evitar duplicados si el servidor alcanzó a procesarlas.
 *
 * @param maxRetries cantidad máxima de reintentos por solicitud.
 * @param initialDelayMillis espera inicial entre reintentos; aumenta progresivamente.
 */
internal class TransientHttpRetryInterceptor(
    private val maxRetries: Int = 2,
    private val initialDelayMillis: Long = 750
) : Interceptor {

    /** Ejecuta la solicitud y aplica reintentos solo a métodos idempotentes. */
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.method() !in SAFE_METHODS) return chain.proceed(request)

        var retries = 0
        while (true) {
            val response = try {
                chain.proceed(request)
            } catch (error: IOException) {
                if (retries >= maxRetries) throw error
                waitBeforeRetry(retries++)
                continue
            }

            if (response.code() !in TRANSIENT_STATUS_CODES || retries >= maxRetries) {
                return response
            }

            response.close()
            waitBeforeRetry(retries++)
        }
    }

    /** Espera con retroceso exponencial antes de volver a consultar el servidor. */
    private fun waitBeforeRetry(retry: Int) {
        val delayMillis = initialDelayMillis * (1L shl retry.coerceAtMost(3))
        TimeUnit.MILLISECONDS.sleep(delayMillis)
    }

    private companion object {
        val SAFE_METHODS = setOf("GET", "HEAD", "OPTIONS")
        val TRANSIENT_STATUS_CODES = setOf(408, 425, 429, 500, 502, 503, 504)
    }
}
