package io.github.brainage04.maidex.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.view.ContextThemeWrapper
import android.view.MotionEvent
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import io.github.brainage04.maidex.ui.theme.MaiDexTheme
import okhttp3.Cache
import okhttp3.CookieJar
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException

private const val CircleDocumentOrigin = "https://maidex-snapshot.invalid/"
private const val CircleLayoutWidth = 480

/**
 * Renders the stored official page; no authenticated WebView or live page is involved.
 *
 * DX NET's pages have no dark styles, so in the dark theme the WebView darkens them algorithmically.
 * On Android 13+ the WebView only does that when its context's theme is dark (`isLightTheme` false),
 * which the activity theme is not when the in-app setting picks Dark over a light system theme; the
 * view therefore gets a context wrapped in a dark platform theme.
 */
@Composable
internal fun OfficialCirclePage(html: String, modifier: Modifier = Modifier) {
    val dark = MaiDexTheme.isDark
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val pageWidth = maxWidth
        var contentHeight by remember(html) { mutableIntStateOf(32) }
        val document = remember(html, pageWidth, dark) {
            val page = Jsoup.parse(html)
            page.select("meta, base, script, iframe, frame, frameset, object, embed, form").remove()
            val head = page.head()
            head.prependElement("meta")
                .attr("name", "viewport")
                .attr(
                    "content",
                    "width=$CircleLayoutWidth,initial-scale=${pageWidth.value / CircleLayoutWidth}," +
                        "minimum-scale=${pageWidth.value / CircleLayoutWidth}," +
                        "maximum-scale=${pageWidth.value / CircleLayoutWidth},user-scalable=no",
                )
            // "only light" opts the page out of algorithmic darkening; the dark theme leaves it unset.
            if (!dark) head.prependElement("meta").attr("name", "color-scheme").attr("content", "only light")
            head.prependElement("meta").attr("http-equiv", "Content-Security-Policy").attr(
                "content",
                "default-src 'none'; " +
                    "style-src 'unsafe-inline' https://maimaidx-eng.com/maimai-mobile/css/ https://maimaidx.jp/maimai-mobile/css/; " +
                    "img-src https://maimaidx-eng.com/maimai-mobile/img/ https://maimaidx.jp/maimai-mobile/img/; " +
                    "form-action 'none'; base-uri 'none'; frame-src 'none'",
            )
            head.appendElement("style").text(
                "html,body{width:480px!important;margin:0!important;padding:0!important;" +
                    "min-height:0!important;height:auto!important;" +
                    (if (dark) "" else "color-scheme:only light!important;") + "}" +
                    "*{-webkit-tap-highlight-color:transparent;}",
            )
            page.outerHtml()
        }
        // A theme change needs a new WebView: the darkening decision comes from its context.
        key(dark) {
            AndroidView(
                modifier = Modifier.fillMaxWidth().height(pageWidth * (contentHeight.toFloat() / CircleLayoutWidth)),
                factory = { context ->
                    val themed = if (dark) {
                        ContextThemeWrapper(context, android.R.style.Theme_Material_NoActionBar)
                    } else {
                        context
                    }
                    CircleSnapshotWebView(themed).apply {
                        setBackgroundColor(if (dark) DarkPageBackground else Color.WHITE)
                        isVerticalScrollBarEnabled = false
                        isHorizontalScrollBarEnabled = false
                        overScrollMode = WebView.OVER_SCROLL_NEVER
                        isNestedScrollingEnabled = false
                        isFocusable = false
                        isFocusableInTouchMode = false
                        isLongClickable = false
                        settings.apply {
                            javaScriptEnabled = false
                            javaScriptCanOpenWindowsAutomatically = false
                            allowFileAccess = false
                            allowContentAccess = false
                            domStorageEnabled = false
                            setGeolocationEnabled(false)
                            setSupportMultipleWindows(false)
                            setSupportZoom(false)
                            builtInZoomControls = false
                            displayZoomControls = false
                            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                            cacheMode = WebSettings.LOAD_DEFAULT
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            textZoom = 100
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                isAlgorithmicDarkeningAllowed = dark
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                @Suppress("DEPRECATION")
                                setForceDark(if (dark) WebSettings.FORCE_DARK_ON else WebSettings.FORCE_DARK_OFF)
                            }
                        }
                        CookieManager.getInstance().setAcceptThirdPartyCookies(this, false)
                        val resources = CircleStaticResources.client(context)
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView, url: String) {
                                view.invalidate()
                            }
                            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest) = true

                            @Deprecated("Used by older WebView implementations")
                            override fun shouldOverrideUrlLoading(view: WebView, url: String) = true

                            override fun shouldInterceptRequest(
                                view: WebView,
                                request: WebResourceRequest,
                            ): WebResourceResponse = if (request.isForMainFrame && request.url.toString() == CircleDocumentOrigin) {
                                WebResourceResponse(
                                    "text/html",
                                    "UTF-8",
                                    ByteArrayInputStream((view as CircleSnapshotWebView).document.orEmpty().toByteArray(Charsets.UTF_8)),
                                )
                            } else if (request.isForMainFrame || request.method != "GET") {
                                blockedCircleResource()
                            } else {
                                circleResource(resources, request.url.toString())
                            }
                        }
                    }
                },
                update = { view ->
                    view.onContentHeight = { height -> contentHeight = height }
                    if (view.document != document) {
                        view.document = document
                        view.resetContentHeight()
                        view.loadUrl(CircleDocumentOrigin)
                    }
                },
                onRelease = { view ->
                    view.onContentHeight = null
                    view.stopLoading()
                    view.removeAllViews()
                    view.destroy()
                },
            )
        }
    }
}

/** Matches the dark window background, so the page does not flash white while it loads. */
private const val DarkPageBackground = 0xFF101417.toInt()

private class CircleSnapshotWebView(context: Context) : WebView(context) {
    var document: String? = null
    var onContentHeight: ((Int) -> Unit)? = null
    private var reportedHeight = 0
    private var heightUpdatePending = false

    fun resetContentHeight() {
        reportedHeight = 0
    }

    // Images and styles can change layout after onPageFinished. Observe native render passes,
    // not JavaScript measurements or a polling loop. One CSS pixel of viewport rounding is ignored.
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!heightUpdatePending && contentHeight > 0 && kotlin.math.abs(contentHeight - reportedHeight) > 1) {
            heightUpdatePending = true
            post {
                heightUpdatePending = false
                if (onContentHeight != null) {
                    reportedHeight = contentHeight
                    onContentHeight?.invoke(reportedHeight)
                }
            }
        }
    }

    // Let the containing LazyColumn own the entire gesture, including long presses and drags.
    override fun dispatchTouchEvent(event: MotionEvent): Boolean = false
}

private object CircleStaticResources {
    private var cachedClient: OkHttpClient? = null

    @Synchronized
    fun client(context: Context): OkHttpClient = cachedClient ?: OkHttpClient.Builder()
        .cookieJar(CookieJar.NO_COOKIES)
        .followRedirects(false)
        .followSslRedirects(false)
        .cache(Cache(File(context.applicationContext.cacheDir, "circle-static-resources"), 20L * 1024 * 1024))
        .build()
        .also { cachedClient = it }
}

private fun blockedCircleResource() = WebResourceResponse(
    "text/plain", "UTF-8", 403, "Blocked", emptyMap(), ByteArrayInputStream(ByteArray(0)),
)

private fun circleResource(client: OkHttpClient, rawUrl: String): WebResourceResponse {
    val url = rawUrl.toHttpUrlOrNull() ?: return blockedCircleResource()
    if (url.scheme != "https" || url.port != 443 || url.username.isNotEmpty() || url.password.isNotEmpty() ||
        (url.host != "maimaidx-eng.com" && url.host != "maimaidx.jp")
    ) return blockedCircleResource()
    val path = url.encodedPath
    if ('%' in path || '\\' in path) return blockedCircleResource()
    val extension = path.substringAfterLast('.', "").lowercase()
    val mime = when {
        path.startsWith("/maimai-mobile/css/") && extension == "css" -> "text/css"
        path.startsWith("/maimai-mobile/img/") -> when (extension) {
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "svg" -> "image/svg+xml"
            "avif" -> "image/avif"
            "ico" -> "image/x-icon"
            else -> return blockedCircleResource()
        }
        else -> return blockedCircleResource()
    }
    // Do not forward WebView headers (especially Cookie/Referer), or follow a redirect outside
    // the static allowlist. The shared HTTP cache contains only these unauthenticated assets.
    return try {
        val response = client.newCall(Request.Builder().url(url).get().build()).execute()
        val body = response.body
        if (!response.isSuccessful || body == null) {
            response.close()
            blockedCircleResource()
        } else {
            WebResourceResponse(mime, if (mime == "text/css") "UTF-8" else null, body.byteStream())
        }
    } catch (_: IOException) {
        blockedCircleResource()
    }
}
