package mx.utng.cala.core.data.model

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

data class Punto(
    val longitud: Double = 0.0,
    val latitud: Double = 0.0
)
