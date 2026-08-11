package mx.utng.cala.core.data.model

/** Modelo de dominio con los datos de un entrenamiento. */
data class Entrenamiento(
    val idEntrenamiento: Int = 0,
    val idUsuario: Int = 0,
    val idRuta: Int? = null,
    val pasos: Int = 0,
    val calorias: Int = 0,
    val distancia: Double = 0.0,
    val fechaInicio: String = "",
    val tiempo: Int = 0,
    val puntoInicio: Punto? = null,
    val puntoFin: Punto? = null
)

/** Coordenada geografica usada como inicio o fin de una ruta. */
data class Punto(
    val longitud: Double = 0.0,
    val latitud: Double = 0.0
)
