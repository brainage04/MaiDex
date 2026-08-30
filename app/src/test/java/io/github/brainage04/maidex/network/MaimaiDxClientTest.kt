package io.github.brainage04.maidex.network

import io.github.brainage04.maidex.data.AccountRegion
import io.github.brainage04.maidex.data.CirclePageInfo
import io.github.brainage04.maidex.data.CirclePageType
import io.github.brainage04.maidex.data.NoteCounts
import io.github.brainage04.maidex.data.Regions
import io.github.brainage04.maidex.data.SongChart
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
              <img class="p_l_10 h_35 f_l" src="/maimai-mobile/img/class/class_rank_s_9f8e.png">
              <div class="p_l_10 f_l f_14">×355</div>
            </div>
            <div>play count of current version : 16</div>
            <div>maimaiDX total play count : 2,203</div>
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
        assertEquals(
            "https://maimaidx-eng.com/maimai-mobile/img/class/class_rank_s_9f8e.png",
            profile.classRankUrl,
        )
        assertEquals("S", profile.friendClass)
        assertEquals(16, profile.currentVersionPlayCount)
        assertEquals(2_203, profile.totalPlayCount)
        assertEquals(123L, profile.importedAt)
    }

    @Test
    fun `map parser keeps only officially completed Chihos`() {
        val document = Jsoup.parse(
            """
            <div class="map_block">
              <div class="map_name_block_inner">トリコロちほー</div>
              <img class="map_comp_img" src="/maimai-mobile/img/map_complete.png">
            </div>
            <div class="map_block">
              <div class="map_name_block_inner">オンゲキちほー9</div>
              <div>525 km remaining</div>
            </div>
            """.trimIndent(),
        )

        assertEquals(setOf("トリコロちほー"), extractCompletedChihoNames(document))
    }

    @Test
    fun `friend matching parser prefers the detailed displayed class`() {
        val document = Jsoup.parse(
            """
            <div class="friend_matching">
              <div>Friend Class: SSS2</div>
              <img src="/maimai-mobile/img/class/class_rank_sss_9f8e.png">
            </div>
            """.trimIndent(),
        )

        assertEquals("SSS2", extractFriendClass(document))
    }

    @Test
    fun `profile parser recognizes current Icon profile picture URLs`() {
        val document = Jsoup.parse(
            """
            <div class="see_through_block">
              <div class="name_block">Player</div>
              <div class="rating_block">16000</div>
              <img src="/maimai-mobile/img/Icon/c5f687e5d0da9696.png">
            </div>
            """.trimIndent(),
            "https://maimaidx-eng.com/maimai-mobile/home/",
        )

        val profile = extractPlayerProfile(document, AccountRegion.INTERNATIONAL)!!

        assertEquals(
            "https://maimaidx-eng.com/maimai-mobile/img/Icon/c5f687e5d0da9696.png",
            profile.avatarUrl,
        )
    }

    @Test
    fun `circle parser keeps profile points rank rewards members and page details`() {
        val document = Jsoup.parse(
            """
            <main class="main_wrapper">
              <img class="title" src="/maimai-mobile/img/title_circle_profile.png">
              <section class="circle_profile">
                <img class="circle_icon" src="/maimai-mobile/img/CircleIcon/test.png">
                <div class="circle_name_block">Test Circle</div>
                <div class="circle_code_block">Circle code: ABCDEFGH</div>
                <div class="circle_leader">Leader: B r a i n a g e</div>
                <div class="circle_comment">Comment: Play together!</div>
                <span class="circle_tag">Beginners welcome</span>
                <div class="circle_class_table">
                  <span>Circle class</span><span>Gold</span>
                </div>
              </section>
              <section class="point_panel">
                <div class="circle_totalpoint_header_for_index">September Circle cumulative points</div>
                <strong>1,500 PT</strong>
                <div>Circle point reset in 12 days</div>
              </section>
              <section class="rank_panel">
                <div class="circle_pointranking_header">Current regional ranking</div>
                <strong>15th</strong>
                <div>2025/09/18 04:00 updated</div>
              </section>
              <div>Next reward in 400 PT</div>
              <div>Members: 2</div>
              <div class="circle_member_row">
                <span class="member_name">B r a i n a g e</span>
                <span>Leader</span><span>900 PT</span>
              </div>
              <div class="circle_member_row">
                <span class="member_name">Other player</span>
                <span>Member</span><span>600 PT</span>
              </div>
              <section class="reward_page">
                <h2>Circle point rewards</h2>
                <div class="circle_reward"><b>500 PT</b><img alt="200 mai-mile" src="/reward.png"></div>
                <div class="circle_reward"><b>2,000 PT</b><img alt="Frame" src="/frame.png"></div>
              </section>
              <table><tr><th>Region</th><td>International</td></tr></table>
            </main>
            """.trimIndent(),
            "https://maimaidx-eng.com/maimai-mobile/circle/",
        )

        val circle = extractCircleData(
            documents = listOf(document),
            playerName = "B r a i n a g e",
            importedAt = 1_758_153_600_000L,
        )!!

        assertEquals("2025-09", circle.month)
        assertEquals("Test Circle", circle.name)
        assertEquals("ABCDEFGH", circle.code)
        assertEquals("B r a i n a g e", circle.leader)
        assertEquals("Play together!", circle.comment)
        assertEquals("Gold", circle.circleClass)
        assertEquals(1_500, circle.totalPoints)
        assertEquals(15, circle.regionalRank)
        assertEquals(12, circle.daysUntilReset)
        assertEquals(400, circle.nextRewardPoints)
        assertEquals(listOf(900, 600), circle.members.map { it.points })
        assertEquals(true, circle.members.first().isCurrentUser)
        assertEquals(listOf(500, 2_000), circle.rewards.map { it.pointsRequired })
        assertEquals(listOf(true, false), circle.rewards.map { it.earned })
        assertEquals("International", circle.information.single { it.label == "Region" }.value)
        assertEquals(true, circle.pages.single().text.contains("Beginners welcome"))
        assertEquals(
            "https://maimaidx-eng.com/maimai-mobile/img/CircleIcon/test.png",
            circle.profileImageUrl,
        )
        assertEquals(CirclePageType.PROFILE, circle.pages.single().type)
        assertEquals("Circle profile", circle.pages.single().title)
    }

    @Test
    fun `circle challenge page has a human label and structured ranking entries`() {
        val profile = Jsoup.parse(
            """
            <main class="main_wrapper">
              <img class="title" src="/maimai-mobile/img/title_circle_profile.png">
              <div class="circle_name_block">Test Circle</div>
            </main>
            """.trimIndent(),
            "https://maimaidx-eng.com/maimai-mobile/circle/profile/",
        )
        val challenge = Jsoup.parse(
            """
            <main class="main_wrapper">
              <img class="title" src="/maimai-mobile/img/title_circle_circlechallenge_ranking.png">
              <div class="ranking_block">
                <strong>Player One</strong>
                <span>100.5000%</span>
              </div>
            </main>
            """.trimIndent(),
            "https://maimaidx-eng.com/maimai-mobile/circle/circleChallenge/ranking/",
        )

        val circle = extractCircleData(listOf(profile, challenge), "Player One", importedAt = 123L)!!
        val page = circle.pages.single { it.type == CirclePageType.CHALLENGE_RANKING }

        assertEquals("Circle Challenge ranking", page.title)
        assertEquals("Player One", page.items.single().label)
        assertEquals("100.5000%", page.items.single().value)
    }

    @Test
    fun `circle parser preserves every recognized read only page`() {
        fun page(path: String, title: String = "", text: String = "") = Jsoup.parse(
            """
            <main class="main_wrapper">
              ${if (title.isBlank()) "" else """<img class="title" src="/img/circle/title/$title.png">"""}
              $text
            </main>
            """.trimIndent(),
            "https://maimaidx-eng.com/maimai-mobile/circle/$path",
        )
        val documents = listOf(
            page("profile/", "title_circle_profile", """<div class="circle_name_block">Test Circle</div>"""),
            page("circleSearch/", "title_circle_serach"),
            page("circleSearch/find/", "title_circle_serach", "Recruiting circle"),
            page("festa/festaRanking", text = "Festa ranking"),
            page("festa/festaHistory", text = "Past Festa"),
            page("circleRankingRule/", "title_circle_ranking_rule", "Ranking rules"),
            page("circleLeave/", "title_circle_leave", "Leave the Circle?"),
        )

        val types = extractCircleData(documents, "Player", importedAt = 123L)!!
            .pages
            .map(CirclePageInfo::type)
            .toSet()

        assertEquals(
            setOf(
                CirclePageType.PROFILE,
                CirclePageType.SEARCH,
                CirclePageType.SEARCH_RESULTS,
                CirclePageType.FESTA_RANKING,
                CirclePageType.FESTA_HISTORY,
                CirclePageType.RANKING_RULE,
                CirclePageType.LEAVE,
            ),
            types,
        )
    }

    @Test
    fun `circle parser returns null when DX NET says the player has no circle`() {
        val document = Jsoup.parse(
            """<main class="main_wrapper">You are currently not in a circle.</main>""",
        )

        assertEquals(null, extractCircleData(listOf(document), "Player", importedAt = 123L))
    }

    @Test
    fun `utage lookup matches catalog mode labels to generic DX NET difficulty`() {
        val chart = testChart(
            title = "宴のテスト",
            type = "utage",
            difficulty = "【宴】",
            level = "13?",
        )

        assertEquals(chart, ChartLookup(listOf(chart)).find("宴のテスト", "utage", "utage", "13"))
    }

    @Test
    fun `utage lookup does not guess between indistinguishable variants`() {
        val variants = listOf(
            testChart("Garakuta Doll Play", "utage", "【宴】", "*", id = 1),
            testChart("Garakuta Doll Play", "utage", "【宴】", "*", id = 2),
        )

        assertEquals(null, ChartLookup(variants).find("Garakuta Doll Play", "utage", "utage", "*"))
    }

}

private fun testChart(
    title: String,
    type: String,
    difficulty: String,
    level: String,
    id: Long = 1,
) = SongChart(
    id = id,
    chartKey = "chart-$id",
    sourceSongId = "song-$id",
    category = "",
    title = title,
    titleRomanized = "",
    titleAliases = "",
    artist = "",
    artistRomanized = "",
    bpm = null,
    imageUrl = "",
    songVersion = "",
    releaseDate = null,
    comment = null,
    type = type,
    difficulty = difficulty,
    level = level,
    levelValue = null,
    constant = null,
    noteDesigner = null,
    noteDesignerRomanized = "",
    noteCounts = NoteCounts(null, null, null, null, null, null),
    regions = Regions(false, false, false, false),
    chartVersion = null,
    isSpecial = type == "utage",
)
