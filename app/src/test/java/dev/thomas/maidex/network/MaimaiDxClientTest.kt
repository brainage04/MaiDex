package dev.thomas.maidex.network

import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Test

class MaimaiDxClientTest {
    @Test
    fun `recent play title excludes displayed level and icons`() {
        val element = Jsoup.parse(
            """
            <div class="basic_block m_5 m_t_17 m_r_60">
              <div class="playlog_level_icon">14+</div>
              <img src="/maimai-mobile/img/music_dx.png">
              AMABIE
            </div>
            """.trimIndent(),
        ).selectFirst(".basic_block")!!

        assertEquals("AMABIE", extractRecentTitle(element))
    }
}
