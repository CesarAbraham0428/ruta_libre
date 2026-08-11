package mx.utng.cala.core.data.model

/** Modelo de una ruta formada por varias coordenadas. */
data class Ruta(
    val idRuta: Int = 0,
    val coordenadas: List<Coordenada> = emptyList()
)

/** Punto geografico que forma parte de una ruta. */
data class Coordenada(
    val longitud: Double = 0.0,
    val latitud: Double = 0.0
)
