package io.github.brainage04.maidex.autologin

import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.util.Locale

sealed interface SignInResult {
    data object Success : SignInResult

    /** DX NET said the SEGA ID or password is wrong. Never retried automatically. */
    data class InvalidCredentials(val message: String) : SignInResult

    /** The gateway showed some other error (maintenance, locked account, …). */
    data class Rejected(val message: String) : SignInResult

    /** The login page no longer looks like what MaiDex understands; credentials were not sent or the outcome is unknown. */
    data class PageChanged(val message: String) : SignInResult
}

/**
 * Performs the SEGA ID form login that maimai DX NET International offers on its Aime gateway
 * (`lng-tgk-aime-gw.am-all.net/common_auth/login`): load the entry page, follow the redirect to
 * the gateway, and post the `sidForm`. Cookies are stored by [http]'s cookie jar.
 *
 * Credentials are only posted to a gateway host in [allowedLoginHosts] over HTTPS, so a changed
 * or hijacked redirect cannot exfiltrate them. Every call makes at most one credential POST.
 */
class SegaIdSignIn(
    private val http: OkHttpClient,
    private val allowedLoginHosts: Set<String> = setOf(DEFAULT_LOGIN_HOST),
    private val requireHttps: Boolean = true,
) {
    /**
     * @param entryUrl DX NET page that redirects unauthenticated visitors to the gateway.
     * @param sitePrefix URL prefix of signed-in DX NET pages.
     */
    fun signIn(entryUrl: String, sitePrefix: String, credentials: DxNetCredentials): SignInResult {
        val (loginUrl, loginPage) = fetch(Request.Builder().url(entryUrl).get().build())
        if (loginUrl.toString().startsWith(sitePrefix)) return SignInResult.Success

        if (requireHttps && loginUrl.scheme != "https" || loginUrl.host !in allowedLoginHosts) {
            return SignInResult.PageChanged(
                "DX NET sent sign-in to an unexpected address (${loginUrl.host}); credentials were not sent",
            )
        }
        val form = loginPage.selectFirst("form#sidForm")
            ?: loginPage.select("form").firstOrNull { it.selectFirst("input[type=password]") != null }
            ?: return SignInResult.PageChanged("The SEGA ID sign-in form was not found on DX NET")
        val usernameField = form.selectFirst("input[name=sid]")
            ?: form.selectFirst("input[type=text][name], input[type=email][name]")
        val passwordField = form.selectFirst("input[type=password][name]")
        if (usernameField == null || passwordField == null) {
            return SignInResult.PageChanged("The SEGA ID sign-in form has changed")
        }
        val actionUrl = form.absUrl("action").ifBlank { loginUrl.toString() }.toHttpUrlOrNull()
        if (actionUrl == null || actionUrl.host != loginUrl.host || actionUrl.scheme != loginUrl.scheme) {
            return SignInResult.PageChanged("The SEGA ID sign-in form posts to an unexpected address")
        }
        if (!form.attr("method").equals("post", ignoreCase = true)) {
            return SignInResult.PageChanged("The SEGA ID sign-in form no longer uses POST")
        }

        val body = FormBody.Builder().apply {
            form.select("input[name]").forEach { input ->
                val name = input.attr("name")
                when {
                    input == usernameField -> add(name, credentials.username)
                    input == passwordField -> add(name, credentials.password)
                    input.attr("type").lowercase(Locale.ROOT) in SKIPPED_INPUT_TYPES -> Unit
                    name == "retention" -> add(name, "1")
                    else -> add(name, input.attr("value"))
                }
            }
        }.build()
        val (resultUrl, resultPage) = fetch(
            Request.Builder()
                .url(actionUrl)
                .post(body)
                .header("Referer", loginUrl.toString())
                .header("Origin", "${loginUrl.scheme}://${loginUrl.host}")
                .build(),
        )
        return classifyResult(resultUrl, resultPage, sitePrefix)
    }

    private fun fetch(request: Request): Pair<HttpUrl, Document> =
        http.newCall(request.newBuilder().header("User-Agent", USER_AGENT).build()).execute().use { response ->
            response.requireSuccess()
            val finalUrl = response.request.url
            finalUrl to Jsoup.parse(response.body?.string().orEmpty(), finalUrl.toString())
        }

    private fun Response.requireSuccess() {
        // Server errors and throttling are transient; surface them as network failures.
        if (!isSuccessful) throw IOException("DX NET sign-in returned HTTP $code")
    }

    companion object {
        const val DEFAULT_LOGIN_HOST = "lng-tgk-aime-gw.am-all.net"
        private val SKIPPED_INPUT_TYPES = setOf("checkbox", "radio", "submit", "button", "image", "file", "reset")
        private val INVALID_CREDENTIAL_PATTERN = Regex(
            "incorrect|invalid|wrong|not (?:be )?(?:found|registered)|正しくありません|間違|誤り",
            RegexOption.IGNORE_CASE,
        )
        internal const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 15) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0 Mobile Safari/537.36"

        internal fun classifyResult(url: HttpUrl, page: Document, sitePrefix: String): SignInResult {
            if (url.toString().startsWith(sitePrefix)) return SignInResult.Success
            // Only the gateway's dedicated error box: `.c-caution` is also used for static notices.
            val message = page.select("#error")
                .text()
                .replace(Regex("""\s+"""), " ")
                .trim()
            return when {
                message.isNotEmpty() && INVALID_CREDENTIAL_PATTERN.containsMatchIn(message) ->
                    SignInResult.InvalidCredentials(message)
                message.isNotEmpty() -> SignInResult.Rejected(message.take(200))
                page.selectFirst("input[type=password]") != null ->
                    SignInResult.PageChanged("DX NET returned to the sign-in form without an explanation")
                else -> SignInResult.PageChanged("DX NET sign-in ended on an unexpected page (${url.host}${url.encodedPath})")
            }
        }
    }
}
