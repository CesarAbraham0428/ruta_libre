package mx.utng.cala.tv.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.utng.cala.core.data.dto.response.ComparacionRendimientoResponse
import mx.utng.cala.core.data.dto.response.DashboardSemanalResponse
import mx.utng.cala.core.data.dto.response.RespuestaDashboardMensual
import mx.utng.cala.core.data.repository.EntrenamientoRepository

/** Periodos que puede mostrar el dashboard de la TV. */
enum class PeriodoDashboard {
    SEMANAL,
    MENSUAL
}

/** Estado observable de datos, carga y errores del dashboard. */
data class EstadoUiDashboard(
    val estaCargando: Boolean = false,
    val semanal: DashboardSemanalResponse? = null,
    val comparacionSemanal: ComparacionRendimientoResponse? = null,
    val mensual: RespuestaDashboardMensual? = null,
    val comparacionMensual: ComparacionRendimientoResponse? = null,
    val periodoSeleccionado: PeriodoDashboard = PeriodoDashboard.SEMANAL,
    val error: String? = null
)

/** Carga metricas del usuario y coordina el cambio de periodo. */
class DashboardViewModel : ViewModel() {

    private val repositorio = EntrenamientoRepository()
    private val _estadoUi = MutableStateFlow(EstadoUiDashboard())
    val estadoUi: StateFlow<EstadoUiDashboard> = _estadoUi

    /** Solicita y publica los totales diarios de la semana actual. */
    fun cargarDashboardSemanal(idUsuario: Int) {
        viewModelScope.launch {
            _estadoUi.value = _estadoUi.value.copy(estaCargando = true, error = null)
            repositorio.getDashboardSemanal(idUsuario).fold(
                onSuccess = {
                    _estadoUi.value = _estadoUi.value.copy(
                        estaCargando = false,
                        semanal = it,
                        error = null
                    )
                },
                onFailure = { _estadoUi.value = _estadoUi.value.copy(estaCargando = false, error = it.message) }
            )
        }
    }

    /** Solicita el cambio porcentual frente a la semana anterior. */
    fun cargarComparacionSemanal(idUsuario: Int) {
        viewModelScope.launch {
            repositorio.getComparacion(idUsuario).fold(
                onSuccess = { _estadoUi.value = _estadoUi.value.copy(comparacionSemanal = it) },
                // La comparacion es complementaria: si falla, se mantienen las metricas principales.
                onFailure = { _estadoUi.value = _estadoUi.value.copy(comparacionSemanal = null) }
            )
        }
    }

    /** Solicita y publica los totales agrupados del mes actual. */
    fun cargarDashboardMensual(idUsuario: Int) {
        viewModelScope.launch {
            _estadoUi.value = _estadoUi.value.copy(estaCargando = true, error = null)
            repositorio.obtenerDashboardMensual(idUsuario).fold(
                onSuccess = {
                    _estadoUi.value = _estadoUi.value.copy(
                        estaCargando = false,
                        mensual = it,
                        error = null
                    )
                },
                onFailure = { _estadoUi.value = _estadoUi.value.copy(estaCargando = false, error = it.message) }
            )
        }
    }

    /** Solicita el cambio porcentual frente al mes anterior. */
    fun cargarComparacionMensual(idUsuario: Int) {
        viewModelScope.launch {
            repositorio.obtenerComparacionMensual(idUsuario).fold(
                onSuccess = { _estadoUi.value = _estadoUi.value.copy(comparacionMensual = it) },
                onFailure = { _estadoUi.value = _estadoUi.value.copy(comparacionMensual = null) }
            )
        }
    }

    /** Cambia el periodo visible y carga sus datos correspondientes. */
    fun cambiarPeriodo(nuevoPeriodo: PeriodoDashboard, idUsuario: Int) {
        _estadoUi.value = _estadoUi.value.copy(periodoSeleccionado = nuevoPeriodo)
        if (nuevoPeriodo == PeriodoDashboard.SEMANAL) {
            cargarDashboardSemanal(idUsuario)
            cargarComparacionSemanal(idUsuario)
        } else {
            cargarDashboardMensual(idUsuario)
            cargarComparacionMensual(idUsuario)
        }
    }

    /** Repite las consultas visibles despues de recuperar la cobertura Wi-Fi. */
    fun reintentar(idUsuario: Int) {
        cambiarPeriodo(_estadoUi.value.periodoSeleccionado, idUsuario)
    }
}
