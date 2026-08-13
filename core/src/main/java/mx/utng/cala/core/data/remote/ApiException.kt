package mx.utng.cala.core.data.remote

/**
 * Error HTTP de la API que conserva el código de respuesta para que cada consumidor
 * pueda distinguir entre un fallo transitorio y uno que requiere intervención.
 *
 * @property statusCode código HTTP devuelto por el backend.
 */
class ApiException(
    val statusCode: Int,
    message: String
) : Exception(message)
