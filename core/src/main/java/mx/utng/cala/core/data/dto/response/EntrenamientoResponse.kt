package mx.utng.cala.core.data.dto.response

/** Entrenamiento completo devuelto por la API. */
data class EntrenamientoResponse(
    val idEntrenamiento: Int,
    val idUsuario: Int,
    val idRuta: Int?,
    val pasos: Int,
    val calorias: Int,
    val distancia: Double,
    val fechaInicio: String,
    val tiempo: Int,
    val puntoInicioLat: Double?,
    val puntoInicioLng: Double?,
    val puntoFinLat: Double?,
    val puntoFinLng: Double?
)

/** Resumen del entrenamiento que aun esta en curso. */
data class EntrenamientoActivoResponse(
    val idEntrenamiento: Int,
    val fechaInicio: String,
    val pasos: Int,
    val calorias: Int,
    val distancia: Double
)
