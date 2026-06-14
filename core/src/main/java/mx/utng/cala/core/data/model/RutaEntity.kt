package mx.utng.cala.core.data.model

data class Ruta(
    val idRuta: Int = 0,
    val coordenadas: List<Coordenada> = emptyList()
)

data class Coordenada(
    val longitud: Double = 0.0,
    val latitud: Double = 0.0
)
