package mx.utng.cala.tv.data.remote

import mx.utng.cala.tv.BuildConfig

/** Contiene la clave y la URL base de YouTube Data API. */
object ConfiguracionYouTube {
    val CLAVE_API = BuildConfig.YOUTUBE_API_KEY
    const val URL_BASE = "https://www.googleapis.com/youtube/v3/"
}
