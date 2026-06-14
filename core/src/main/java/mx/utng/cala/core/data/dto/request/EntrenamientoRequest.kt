package mx.utng.cala.core.data.dto.request

import mx.utng.cala.core.data.model.Coordenada
import mx.utng.cala.core.data.model.Punto

data class IniciarEntrenamientoRequest(
    val idUsuario: Int
)

data class FinalizarEntrenamientoRequest(
    val idEntrenamiento: Int,
    val pasos: Int,
    val calorias: Int,
    val distancia: Double,
    val tiempo: Int,
    val coordenadas: List<Coordenada>,
    val puntoInicio: Punto,
    val puntoFin: Punto
)
