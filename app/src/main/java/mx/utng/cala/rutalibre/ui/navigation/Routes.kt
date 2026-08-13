package mx.utng.cala.rutalibre.ui.navigation

/** Define las rutas de navegación y sus constructores con parámetros. */
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val PESO_INICIAL = "peso_inicial"
    const val ENTRENAMIENTO = "entrenamiento/{idEntrenamiento}"
    const val RESUMEN = "resumen/{idEntrenamiento}"
    const val HISTORIAL = "historial"
    const val METAS = "metas"
    const val CREAR_META = "crear_meta"
    const val EDITAR_META = "editar_meta/{idMeta}"
    const val GRUPOS = "grupos"
    const val PERFIL = "perfil"
    const val VINCULAR_DISPOSITIVO = "vincular_dispositivo"
    const val DISPOSITIVOS = "dispositivos"
    const val ACERCA_DE_LA_APP = "acerca_de_la_app"
    const val DETALLE_GRUPO = "detalle_grupo/{idGrupo}/{nombreGrupo}"

    /** Construye la ruta del entrenamiento seleccionado. */
    fun entrenamiento(id: Int) = "entrenamiento/$id"

    /** Construye la ruta del resumen de un entrenamiento. */
    fun resumen(id: Int) = "resumen/$id"

    /** Construye la ruta para editar una meta específica. */
    fun editarMeta(id: Int) = "editar_meta/$id"

    /** Construye la ruta del detalle de un grupo y codifica sus datos dinámicos. */
    fun detalleGrupo(idGrupo: Int, nombreGrupo: String) = "detalle_grupo/$idGrupo/$nombreGrupo"
}
