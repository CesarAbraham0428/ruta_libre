package mx.utng.cala.tv.ui.screens.videos

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.media.AudioManager
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Reproductor aislado de Compose para evitar errores de composición de video entre
 * WebView y AndroidView en algunos dispositivos y emuladores Android TV.
 */
class ReproductorVideoActivity : Activity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    /** Configura la reproduccion YouTube en pantalla completa y con D-pad. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        volumeControlStream = AudioManager.STREAM_MUSIC

        val idVideo = intent.getStringExtra(EXTRA_ID_VIDEO).orEmpty()
        val tituloVideo = intent.getStringExtra(EXTRA_TITULO_VIDEO).orEmpty()
        if (idVideo.isBlank()) {
            finish()
            return
        }

        val fondo = Color.rgb(3, 5, 24)
        val raiz = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(fondo)
            setPadding(24.dp, 18.dp, 24.dp, 18.dp)
        }

        val cabecera = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val volver = Button(this).apply {
            text = "←  Volver a videos"
            isAllCaps = false
            setOnClickListener { finish() }
        }
        val titulo = TextView(this).apply {
            text = tituloVideo
            setTextColor(Color.WHITE)
            textSize = 22f
            maxLines = 1
            gravity = Gravity.CENTER_VERTICAL
            setPadding(24.dp, 0, 0, 0)
        }
        val activarSonido = Button(this).apply {
            text = "🔊  Activar sonido"
            isAllCaps = false
            setOnClickListener {
                (getSystemService(AUDIO_SERVICE) as AudioManager).adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_UNMUTE,
                    0
                )
                if (::webView.isInitialized) {
                    webView.evaluateJavascript(
                        """
                        (function() {
                            var video = document.querySelector('video');
                            if (!video) return 'video-no-encontrado';
                            video.muted = false;
                            video.volume = 1.0;
                            video.play();
                            return 'sonido-activado';
                        })();
                        """.trimIndent(),
                        null
                    )
                    webView.requestFocus()
                }
            }
        }
        cabecera.addView(
            volver,
            LinearLayout.LayoutParams(190.dp, 48.dp)
        )
        cabecera.addView(
            titulo,
            LinearLayout.LayoutParams(0, 48.dp, 1f)
        )
        cabecera.addView(
            activarSonido,
            LinearLayout.LayoutParams(180.dp, 48.dp)
        )
        raiz.addView(
            cabecera,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 48.dp)
        )

        val contenedorVideo = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
        }
        webView = WebView(this).apply {
            webViewClient = WebViewClient()
            webChromeClient = object : WebChromeClient() {
                /** Concede al WebView unicamente el permiso multimedia necesario. */
                override fun onPermissionRequest(request: PermissionRequest) {
                    val permitidos = request.resources.filter {
                        it == PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID
                    }
                    post {
                        if (permitidos.isNotEmpty()) request.grant(permitidos.toTypedArray())
                        else request.deny()
                    }
                }
            }
            isFocusable = true
            isFocusableInTouchMode = true
            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
            setBackgroundColor(Color.BLACK)
            settings.apply {
                javaScriptEnabled = true
                mediaPlaybackRequiresUserGesture = false
                domStorageEnabled = true
                databaseEnabled = true
                useWideViewPort = true
                loadWithOverviewMode = true
                allowFileAccess = false
                allowContentAccess = false
            }
        }
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }
        contenedorVideo.addView(
            webView,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        raiz.addView(
            contenedorVideo,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f).apply {
                topMargin = 16.dp
            }
        )

        setContentView(raiz)

        val url = buildString {
            append("https://www.youtube.com/embed/")
            append(idVideo)
            append("?autoplay=1&playsinline=1&controls=1&rel=0")
            append("&enablejsapi=1&origin=https%3A%2F%2Frutalibre.local")
        }
        webView.loadUrl(url, mapOf("Referer" to "https://rutalibre.local/"))
        volver.requestFocus()
    }

    /** Libera el WebView para evitar fugas y detener la reproduccion. */
    override fun onDestroy() {
        if (::webView.isInitialized) {
            webView.stopLoading()
            webView.loadUrl("about:blank")
            webView.destroy()
        }
        super.onDestroy()
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()

    /** Claves usadas para enviar los datos del video a la actividad. */
    companion object {
        const val EXTRA_ID_VIDEO = "id_video"
        const val EXTRA_TITULO_VIDEO = "titulo_video"
    }
}
