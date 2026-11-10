package io.github.brainage04.maidex.network

import io.github.brainage04.maidex.data.AccountRegion
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/** Maintenance is reported as itself; only real expiries say "login expired". */
class DxNetMaintenanceTest {
    private val region = AccountRegion.INTERNATIONAL

    @Test
    fun `DX NET's daily 503 maintenance page is maintenance, not an expired login`() {
        val failure = assertThrows(DxNetMaintenanceException::class.java) {
            checkDxNetResponse(region, "https://maimaidx-eng.com/maimai-mobile/home/".toHttpUrl(), 503, DX_NET_503_PAGE)
        }
        assertEquals(DX_NET_MAINTENANCE_MESSAGE, failure.message)
    }

    @Test
    fun `redirect to the Aime gateway's maintenance page is maintenance, not an expired login`() {
        assertThrows(DxNetMaintenanceException::class.java) {
            checkDxNetResponse(region, GATEWAY_MAINTENANCE_URL.toHttpUrl(), 200, GATEWAY_MAINTENANCE_PAGE)
        }
    }

    @Test
    fun `redirect to the gateway sign-in page is still an expired login`() {
        val failure = assertThrows(AuthenticationRequiredException::class.java) {
            checkDxNetResponse(
                region,
                "https://lng-tgk-aime-gw.am-all.net/common_auth/login?site_id=maimaidxex".toHttpUrl(),
                200,
                "<html><form id=\"sidForm\"><input type=\"password\" name=\"password\"></form></html>",
            )
        }
        assertEquals("DX NET login expired; sign in again", failure.message)
    }

    @Test
    fun `DX NET's session error page is still an expired session`() {
        val failure = assertThrows(AuthenticationRequiredException::class.java) {
            checkDxNetResponse(
                region,
                "https://maimaidx-eng.com/maimai-mobile/error/".toHttpUrl(),
                200,
                "<html><img src=\"/img/title_error.png\"><div>ERROR CODE：100001</div></html>",
            )
        }
        assertEquals("DX NET session expired; sign in again", failure.message)
    }

    @Test
    fun `other server errors are neither maintenance nor an expired login`() {
        val failure = assertThrows(IllegalStateException::class.java) {
            checkDxNetResponse(region, "https://maimaidx-eng.com/maimai-mobile/home/".toHttpUrl(), 503, "<html>Busy</html>")
        }
        assertTrue(failure !is AuthenticationRequiredException)
        assertEquals("DX NET returned HTTP 503", failure.message)
    }

    @Test
    fun `a normal page is returned as a document`() {
        val document = checkDxNetResponse(
            region,
            "https://maimaidx-eng.com/maimai-mobile/home/".toHttpUrl(),
            200,
            "<html><body><div class=\"name_block\">PLAYER</div></body></html>",
        )
        assertEquals("PLAYER", document.selectFirst(".name_block")?.text())
    }

    companion object {
        const val GATEWAY_MAINTENANCE_URL = "https://lng-tgk-aime-gw.am-all.net/common_auth/maintenance.html"

        /** Text of the gateway page (fetched 10 Nov 2026), markup trimmed. */
        const val GATEWAY_MAINTENANCE_PAGE =
            "<html><head><title>メンテナンス</title></head><body><p>現在メンテナンスを行っております。</p></body></html>"

        /** Text of DX NET's HTTP 503 page during the 02:00-03:00 AEST window (10 Nov 2026), markup trimmed. */
        const val DX_NET_503_PAGE =
            "<html><head><title>maimai DX NET</title></head><body>" +
                "<p>Sorry, servers are under maintenance.</p>" +
                "<p>maimai DX NET is under maintenance currently.</p>" +
                "<p>Maintenance is performed at the following times:</p></body></html>"
    }
}
