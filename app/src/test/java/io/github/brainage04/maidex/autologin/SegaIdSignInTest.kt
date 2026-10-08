package io.github.brainage04.maidex.autologin

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.net.URLDecoder

class SegaIdSignInTest {
    private val server = MockWebServer()
    private val posts = mutableListOf<Map<String, String>>()
    private var loginPage = LOGIN_PAGE
    private var loginStatus = 200

    private val cookies = mutableMapOf<String, Cookie>()
    private val http = OkHttpClient.Builder()
        .cookieJar(object : CookieJar {
            override fun loadForRequest(url: HttpUrl) = cookies.values.filter { it.matches(url) }
            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                cookies.forEach { this@SegaIdSignInTest.cookies[it.name] = it }
            }
        })
        .build()

    @Before
    fun setUp() {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.path.orEmpty()
                return when {
                    path == "/maimai-mobile/" && request.getHeader("Cookie")?.contains("userId=signed-in") == true ->
                        redirect("/maimai-mobile/home/")
                    path == "/maimai-mobile/" -> redirect("/common_auth/login?site_id=maimaidxex")
                    path.startsWith("/maimai-mobile/?ssid=") ->
                        redirect("/maimai-mobile/home/").addHeader("Set-Cookie", "userId=signed-in; Path=/")
                    path == "/maimai-mobile/home/" -> MockResponse().setBody("<html>home</html>")
                    path.startsWith("/common_auth/login?") -> MockResponse()
                        .setResponseCode(loginStatus)
                        .addHeader("Set-Cookie", "JSESSIONID=abc; Path=/common_auth")
                        .setBody(loginPage)
                    path == "/common_auth/login/sid" && request.method == "POST" -> {
                        val form = parseForm(request.body.readUtf8())
                        posts += form
                        val hasSession = request.getHeader("Cookie")?.contains("JSESSIONID=abc") == true
                        if (hasSession && form["sid"] == "player" && form["password"] == "right") {
                            redirect("/maimai-mobile/?ssid=xyz")
                        } else {
                            loginPage = LOGIN_ERROR_PAGE
                            redirect("/common_auth/login?site_id=maimaidxex&alof=0")
                        }
                    }
                    else -> MockResponse().setResponseCode(404)
                }
            }
        }
        server.start()
    }

    @After
    fun tearDown() = server.shutdown()

    private fun signIn(
        username: String = "player",
        password: String = "right",
        allowedHosts: Set<String> = setOf(server.hostName),
    ) = SegaIdSignIn(http, allowedHosts, requireHttps = false).signIn(
        entryUrl = server.url("/maimai-mobile/").toString(),
        sitePrefix = server.url("/maimai-mobile/").toString(),
        credentials = DxNetCredentials(username, password),
    )

    @Test
    fun postsSegaIdFormAndEndsSignedIn() {
        assertEquals(SignInResult.Success, signIn())

        assertEquals(1, posts.size)
        val form = posts.single()
        assertEquals("player", form["sid"])
        assertEquals("right", form["password"])
        assertEquals("1", form["retention"])
        assertFalse("terms checkbox must not be posted", form.containsKey("agree"))
        assertEquals("signed-in", cookies["userId"]?.value)
    }

    @Test
    fun wrongPasswordIsReportedAsInvalidCredentialsWithSingleAttempt() {
        val result = signIn(password = "wrong")

        assertTrue(result is SignInResult.InvalidCredentials)
        assertEquals("SEGA ID or password is incorrect.", (result as SignInResult.InvalidCredentials).message)
        assertEquals(1, posts.size)
    }

    @Test
    fun otherGatewayErrorsAreNotTreatedAsWrongPassword() {
        loginPage = LOGIN_PAGE
        val maintenance = LOGIN_ERROR_PAGE.replace("SEGA ID or password is incorrect.", "Under maintenance.")
        server.dispatcher.let { original ->
            server.dispatcher = object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val response = original.dispatch(request)
                    if (request.method == "POST") loginPage = maintenance
                    return response
                }
            }
        }

        val result = signIn(password = "wrong")

        assertEquals(SignInResult.Rejected("Under maintenance."), result)
    }

    @Test
    fun refusesToSendCredentialsToUnexpectedHost() {
        val result = signIn(allowedHosts = setOf(SegaIdSignIn.DEFAULT_LOGIN_HOST))

        assertTrue(result is SignInResult.PageChanged)
        assertTrue(posts.isEmpty())
    }

    @Test
    fun changedLoginPageIsDetectedWithoutPosting() {
        loginPage = "<html><body><p>New login experience</p></body></html>"

        val result = signIn()

        assertTrue(result is SignInResult.PageChanged)
        assertTrue(posts.isEmpty())
    }

    @Test
    fun formPostingElsewhereIsRefused() {
        loginPage = LOGIN_PAGE.replace("action=\"/common_auth/login/sid\"", "action=\"https://evil.example/steal\"")

        val result = signIn()

        assertTrue(result is SignInResult.PageChanged)
        assertTrue(posts.isEmpty())
    }

    @Test
    fun serverErrorsSurfaceAsNetworkFailures() {
        loginStatus = 503
        try {
            signIn()
            fail("Expected IOException")
        } catch (expected: IOException) {
            assertTrue(expected.message.orEmpty().contains("503"))
        }
        assertTrue(posts.isEmpty())
    }

    @Test
    fun existingSessionNeedsNoCredentials() {
        cookies["userId"] = Cookie.parse(server.url("/"), "userId=signed-in; Path=/")!!

        assertEquals(SignInResult.Success, signIn())
        assertTrue(posts.isEmpty())
    }

    private fun redirect(location: String) = MockResponse().setResponseCode(302).addHeader("Location", location)

    private fun parseForm(body: String): Map<String, String> = body.split('&')
        .filter(String::isNotBlank)
        .associate { pair ->
            val (name, value) = pair.split('=', limit = 2).let { it[0] to it.getOrElse(1) { "" } }
            URLDecoder.decode(name, "UTF-8") to URLDecoder.decode(value, "UTF-8")
        }

    private companion object {
        /** Trimmed copy of the real gateway markup (lng-tgk-aime-gw.am-all.net/common_auth/login, 2026-10). */
        val LOGIN_PAGE = """
            <html><body>
            <section class="l-contents__inner">
              <p class="c-caution">* Login information will not be posted to the linked account without permission.</p>
              <input type="checkbox" id="retention-c" class="retention-c" value="1" checked>
              <button type="submit" name="login" value="twitter" data-target="button_sns">X / Twitter</button>
              <form action="/common_auth/login/sid" method="post" class="c-form js-validate" id="sidForm">
                <input type="hidden" name="retention" id="retention" value="0">
                <input type="checkbox" name="agree" class="c-form__checkbox js-agree">
                <input type="text" id="sid" name="sid" class="c-form__input isEmpty">
                <input type="password" id="password" name="password" autocomplete="new-password">
                <button type="submit" name="" class="c-button--login js-agreeSubmit" id="btnSubmit" value="Login">Login</button>
              </form>
            </section>
            </body></html>
        """.trimIndent()

        val LOGIN_ERROR_PAGE = LOGIN_PAGE.replace(
            "<section class=\"l-contents__inner\">",
            "<section class=\"l-contents__inner\"><div class=\"c-caution\" id=\"error\">" +
                "<ul id=\"error-ui\"><p>SEGA ID or password is incorrect.</p></ul></div>",
        )
    }
}
