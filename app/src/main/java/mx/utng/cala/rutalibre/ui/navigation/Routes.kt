package mx.utng.cala.rutalibre.ui.navigation

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val ENTRENAMIENTO = "entrenamiento/{idEntrenamiento}"
    const val RESUMEN = "resumen/{idEntrenamiento}"
    const val METAS = "metas"
    const val CREAR_META = "crear_meta"
    const val EDITAR_META = "editar_meta/{idMeta}"
    const val GRUPOS = "grupos"
    const val PERFIL = "perfil"

    fun entrenamiento(id: Int) = "entrenamiento/$id"
    fun resumen(id: Int) = "resumen/$id"
    fun editarMeta(id: Int) = "editar_meta/$id"
}
