package io.github.brainage04.maidex.network

import io.github.brainage04.maidex.data.AccountRegion
import io.github.brainage04.maidex.data.CirclePageInfo
import io.github.brainage04.maidex.data.CirclePageType
import io.github.brainage04.maidex.data.NoteCounts
import io.github.brainage04.maidex.data.Regions
import io.github.brainage04.maidex.data.SongChart
import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MaimaiDxClientTest {
    @Test
    fun `live Circle layers and numeric history survive without account controls`() {
        val document = Jsoup.parse(
            """
            <link rel="stylesheet" href="/maimai-mobile/css/common.css?ver=1.65?20250401">
            <link rel="stylesheet" href="/maimai-mobile/css/unique.css?ver=1.65?20250902">
            <div class="wrapper main_wrapper t_c">
              <header>Site header</header>
              <div class="m_b_10 f_0"><a href="/account"><img src="/maimai-mobile/img/menu_sub_circle_home.png"></a></div>
              <img class="title m_10" src="/maimai-mobile/img/circle/title/title_circle_profile.png">
              <div class="h_270 p_r">
                <div class="circle_profile_character_bg"><img src="/maimai-mobile/img/circle/profile/UI_circle_profile_CharaBase.png"></div>
                <div class="circle_profile_character"><img src="/maimai-mobile/img/CircleProfile/Character/example.png"></div>
                <div class="circle_profile_bg"><img src="/maimai-mobile/img/CircleProfile/Background/example.png"></div>
                <div class="circle_profile_class"><img src="/maimai-mobile/img/circle/profile/circle_profile_color_gold.png"></div>
                <div class="circle_profile_user_name_frame"><img src="/maimai-mobile/img/circle/profile/circle_profile_username_member.png"></div>
                <div class="circle_profile_tag1 circle_profile_tag_bg"><img src="/maimai-mobile/img/circle/profile/circle_profile_tag_red.png"></div>
                <div class="circle_profile_circle_name"><span>Example Circle</span></div>
                <div class="circle_profile_circle_code"><span>TEST1234</span></div>
                <div class="circle_profile_user_name"><span>Example Player</span></div>
                <div class="circle_profile_comment"><span>Play together!</span></div>
                <div class="circle_profile_tag1 circle_profile_tag_text"><span style="font-size: 15px;">Friend Circle</span></div>
              </div>
              <div class="town_block">
                <div class="circle_totalpoint_block"><div class="h_90">
                  <div class="circle_totalpoint_header_for_index">Circle Total Points for September</div>
                  <div class="circle_totalpoint_point"><span>1,250</span><span> PT</span></div>
                </div></div>
                <div><span>21</span> days until Circle Points reset</div>
                <div class="circle_pointranking_block"><div class="h_90">
                  <div class="circle_pointranking_header">Current Ranking for September</div>
                  <div class="circle_pointranking_point"><span>Rank </span><span>42</span></div>
                </div></div>
              </div>
              <div class="circle_pointreward_block">Next Reward <span>750</span>PT</div>
              <div class="circle_challenge_chosemember_block">
                <img src="/maimai-mobile/img/Music/example.png">
                <form action="/change" method="post"><input type="hidden" name="token" value="private-token">
                  <button type="submit" class="music_basic_btn" onclick="submit()">Ranking</button>
                </form>
              </div>
              <img class="title" src="/maimai-mobile/img/circle/title/title_circle_festa.png">
              <div class="container">The CiRCLE FESTA is not currently being held.</div>
              <div>Past Circle Festa: May 2026</div>
              <footer>Site footer</footer>
            </div>
            """.trimIndent(),
            "https://maimaidx-eng.com/maimai-mobile/circle/",
        )
        val circle = extractCircleData(
            listOf(document),
            "Example Player",
            importedAt = java.time.Instant.parse("2026-09-10T00:00:00Z").toEpochMilli(),
        )!!
        assertEquals("2026-09", circle.month)
        assertEquals("Play together!", circle.comment)
        assertEquals(listOf("Friend Circle"), circle.tags)
        assertEquals("https://maimaidx-eng.com/maimai-mobile/img/CircleProfile/Character/example.png", circle.characterUrl)
        assertEquals(1250, circle.totalPoints)
        assertEquals(42, circle.regionalRank)
        assertEquals(21, circle.daysUntilReset)
        assertEquals(750, circle.nextRewardPoints)
        val snapshot = Jsoup.parse(circle.pages.single().html)
        listOf(
            ".circle_profile_character_bg img", ".circle_profile_character img", ".circle_profile_bg img",
            ".circle_profile_class img", ".circle_profile_user_name_frame img", ".circle_profile_tag_bg img",
            ".circle_profile_tag_text", ".circle_totalpoint_block", ".circle_pointranking_block",
            ".circle_pointreward_block", ".circle_challenge_chosemember_block", "img[src*=title_circle_festa]",
        ).forEach { selector -> assertTrue(selector, snapshot.selectFirst(selector) != null) }
        assertTrue(snapshot.text().contains("The CiRCLE FESTA"))
        assertEquals("Ranking", snapshot.selectFirst("button[type=button]")?.text())
        assertEquals(2, snapshot.select("link[rel=stylesheet]").size)
        assertFalse(snapshot.outerHtml().contains("private-token"))
        assertTrue(snapshot.select("form, input, header, footer, [onclick], img[src*=menu_sub]").isEmpty())
    }

    @Test
    fun `Circle snapshot refuses active markup and nonstatic resource tricks`() {
        val snapshot = Jsoup.parse(sanitizedCircleHtml(Jsoup.parse(
            """
            <link rel="stylesheet" href="https://evil.test/maimai-mobile/css/common.css">
            <link rel="stylesheet" href="/maimai-mobile/css/common.css?token=private-token">
            <main class="main_wrapper">
              <script>private-script</script><iframe src="/account">private-frame</iframe>
              <div hidden>private-hidden</div><div style="display:none">private-hidden-style</div>
              <input value="private-input"><textarea>private-textarea</textarea>
              <a href="javascript:alert(1)" data-token="private-data" onmouseover="alert(1)">Visible link</a>
              <span style="font-size:15px;background:url(https://evil.test/leak);width:expression(alert(1))">Safe text</span>
              <img src="https://maimaidx-eng.com.evil.test/maimai-mobile/img/a.png">
              <img src="https://maimaidx-eng.com@evil.test/maimai-mobile/img/a.png">
              <img src="/maimai-mobile/img/../account.png">
              <img src="/maimai-mobile/img/%2e%2e/account.png">
              <img src="/maimai-mobile/img/a.svg">
              <img src="data:image/png;base64,AAAA">
              <img src="/maimai-mobile/img/safe.png?token=private-token" onerror="alert(1)">
              <svg><script>alert(1)</script></svg>
            </main>
            """.trimIndent(),
            "https://maimaidx-eng.com/maimai-mobile/circle/",
        )))
        assertEquals(listOf("https://maimaidx-eng.com/maimai-mobile/img/safe.png"), snapshot.select("img").map { it.attr("src") })
        assertEquals("https://maimaidx-eng.com/maimai-mobile/css/common.css", snapshot.selectFirst("link")?.attr("href"))
        assertEquals("Visible link", snapshot.selectFirst("a")?.text())
        assertEquals("font-size:15px", snapshot.selectFirst("span")?.attr("style"))
        assertTrue(snapshot.select("script, iframe, input, textarea, svg, [href^=javascript], [onerror], [onmouseover], [data-token]").isEmpty())
        assertFalse(snapshot.outerHtml().contains("private-"))
        assertFalse(snapshot.outerHtml().contains("evil.test"))
    }

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
              <img class="p_l_10 h_35 f_l" src="/maimai-mobile/img/class/class_rank_s_00ZqZmdpb8.png">
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
            "https://maimaidx-eng.com/maimai-mobile/img/class/class_rank_s_00ZqZmdpb8.png",
            profile.classRankUrl,
        )
        assertEquals("B5", profile.friendClass)
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
    fun `profile class uses numeric badge ID and ignores other players`() {
        fun profileClass(filename: String): String {
            val document = Jsoup.parse(
                """
                <div class="see_through_block">
                  <div class="name_block">Player</div>
                  <div class="rating_block">16000</div>
                  <img class="h_35 f_l" src="/maimai-mobile/img/course/course_rank_10.png">
                  <img class="p_l_10 h_35 f_l" src="/maimai-mobile/img/class/$filename.png">
                </div>
                <div class="see_through_block">
                  <div class="name_block">Other player</div>
                  <img class="p_l_10 h_35 f_l" src="/maimai-mobile/img/class/class_rank_s_11asset.png">
                </div>
                """.trimIndent(),
                "https://maimaidx-eng.com/maimai-mobile/home/",
            )
            return extractPlayerProfile(document, AccountRegion.INTERNATIONAL)!!.friendClass
        }

        assertEquals("SS2", profileClass("class_rank_s_18Hixuxin0"))
        assertEquals("S3", profileClass("class_rank_l_12ruJTrWoU"))
        assertEquals("SSS1", profileClass("class_rank_s_24asset"))
        assertEquals("LEGEND", profileClass("class_rank_s_25asset"))
        assertEquals("", profileClass("class_rank_s_99asset"))
        assertEquals("", profileClass("class_rank_s_unknown"))
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
    fun `circle page excludes navigation without removing matching circle content`() {
        val document = Jsoup.parse(
            """
            <main class="main_wrapper">
              <nav class="spmenu_navigation">
                <div class="basic_block">FRIENDS navigation<img src="/img/friends.png"></div>
              </nav>
              <div class="menu"><div class="basic_block">FRIENDS navigation</div></div>
              <div class="m_t_5 m_b_10 p_r t_l f_0">
                <a href="/maimai-mobile/friend/"><img src="/maimai-mobile/img/menu_sub_friend_list.png"></a>
              </div>
              <div class="circle_name_block">FRIENDS circle</div>
              <div class="circle_comment">Play with FRIENDS</div>
              <div class="basic_block"><b>Reward</b>500 PT<img src="/reward.png"></div>
            </main>
            """.trimIndent(),
            "https://maimaidx-eng.com/maimai-mobile/circle/",
        )

        val page = extractCircleData(listOf(document), "Player", importedAt = 123L)!!.pages.single()

        assertTrue(page.text.contains("FRIENDS circle"))
        assertTrue(page.text.contains("Play with FRIENDS"))
        assertFalse(page.text.contains("navigation"))
        assertEquals(listOf("Reward"), page.items.map { it.label })
        assertEquals(listOf("https://maimaidx-eng.com/reward.png"), page.imageUrls)
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
