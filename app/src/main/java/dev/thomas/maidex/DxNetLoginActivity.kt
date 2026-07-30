package dev.thomas.maidex

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import dev.thomas.maidex.data.AccountRegion

class DxNetLoginActivity : ComponentActivity() {
    private lateinit var webView: WebView
    private val region: AccountRegion by lazy {
        intent.getStringExtra(EXTRA_REGION)
            ?.let { value -> AccountRegion.entries.firstOrNull { it.name == value } }
            ?: AccountRegion.INTERNATIONAL
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "${region.label} maimai DX NET"

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
        }
        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(24, 12, 12, 12)
            setBackgroundColor(Color.WHITE)
        }
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { view, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.setPadding(24, statusBar.top + 12, 12, 12)
            insets
        }
        toolbar.addView(
            TextView(this).apply {
                text = "${region.label} DX NET sign-in"
                textSize = 18f
                setTextColor(Color.BLACK)
            },
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f),
        )
        toolbar.addView(Button(this).apply {
            text = "Close"
            setOnClickListener { finish() }
        })
        container.addView(
            toolbar,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ),
        )

        if (BuildConfig.DEBUG) WebView.setWebContentsDebuggingEnabled(true)
        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadsImagesAutomatically = true
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.setSupportMultipleWindows(false)
            settings.userAgentString = settings.userAgentString
                .replace("; wv", "")
                .replace(" Version/4.0", "")
            val loginWebView = this
            CookieManager.getInstance().apply {
                setAcceptCookie(true)
                setAcceptThirdPartyCookies(loginWebView, true)
            }
            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    val uri = request.url
                    if (uri.scheme == "http" || uri.scheme == "https") return false
                    openExternalOrFallback(view, uri)
                    return true
                }

                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)
                    CookieManager.getInstance().flush()
                    if (url.startsWith("${region.baseUrl}/maimai-mobile/home/")) {
                        setResult(RESULT_OK)
                        finish()
                    }
                }
            }
            loadUrl(region.loginUrl)
        }
        container.addView(
            webView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f,
            ),
        )
        setContentView(container)

        onBackPressedDispatcher.addCallback(this) {
            if (webView.canGoBack()) webView.goBack() else finish()
        }
    }

    private fun openExternalOrFallback(webView: WebView, uri: Uri) {
        if (uri.scheme == "intent") {
            val intentUri = runCatching { Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME) }.getOrNull()
            val fallback = intentUri?.getStringExtra("browser_fallback_url")
            if (!fallback.isNullOrBlank()) {
                webView.loadUrl(fallback)
                return
            }
            if (intentUri != null && runCatching { startActivity(intentUri) }.isSuccess) return
        }
        runCatching { startActivity(Intent(Intent.ACTION_VIEW, uri)) }
    }

    override fun onDestroy() {
        if (::webView.isInitialized) webView.destroy()
        super.onDestroy()
    }

    companion object {
        private const val EXTRA_REGION = "region"

        fun intent(context: Context, region: AccountRegion): Intent =
            Intent(context, DxNetLoginActivity::class.java).putExtra(EXTRA_REGION, region.name)
    }
}
