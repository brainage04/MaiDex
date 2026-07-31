package dev.thomas.maidex.network

import dev.thomas.maidex.data.AccountRegion
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
    @Test
    fun `profile parser keeps official DX NET card details`() {
        val document = Jsoup.parse(
            """
            <div class="see_through_block">
              <img class="w_112 f_l" src="/maimai-mobile/img/avatar.png">
              <div class="trophy_block trophy_gold p_3 t_c f_0">
                <div class="trophy_inner_block f_13">INFiNiTE ENERZY -Overdoze-</div>
              </div>
              <div class="name_block f_l f_16">B r a i n a g e</div>
              <div class="rating_block">16070</div>
              <img class="h_35 f_l" src="/maimai-mobile/img/course.png">
              <img class="p_l_10 h_35 f_l" src="/maimai-mobile/img/class.png">
              <div class="p_l_10 f_l f_14">×355</div>
            </div>
            """.trimIndent(),
            "https://maimaidx-eng.com/maimai-mobile/home/",
        )

        val profile = extractPlayerProfile(
            document,
            AccountRegion.INTERNATIONAL,
            importedAt = 123L,
        )!!

        assertEquals("B r a i n a g e", profile.name)
        assertEquals(16070, profile.officialRating)
        assertEquals("INFiNiTE ENERZY -Overdoze-", profile.title)
        assertEquals(355, profile.starCount)
        assertEquals("https://maimaidx-eng.com/maimai-mobile/img/avatar.png", profile.avatarUrl)
        assertEquals("gold", profile.titleRarity)
        assertEquals("https://maimaidx-eng.com/maimai-mobile/img/course.png", profile.courseRankUrl)
        assertEquals("https://maimaidx-eng.com/maimai-mobile/img/class.png", profile.classRankUrl)
        assertEquals(123L, profile.importedAt)
    }

}
