package io.github.brainage04.maidex.network

import android.webkit.CookieManager
import io.github.brainage04.maidex.data.AccountRegion
import io.github.brainage04.maidex.data.CircleData
import io.github.brainage04.maidex.data.CircleInfoItem
import io.github.brainage04.maidex.data.CircleMember
import io.github.brainage04.maidex.data.CirclePageInfo
import io.github.brainage04.maidex.data.CirclePageItem
import io.github.brainage04.maidex.data.CirclePageType
import io.github.brainage04.maidex.data.CircleReward
import io.github.brainage04.maidex.data.ComboMedal
import io.github.brainage04.maidex.data.Grade
import io.github.brainage04.maidex.data.ImportResult
import io.github.brainage04.maidex.data.JudgeCounts
import io.github.brainage04.maidex.data.JudgmentTable
import io.github.brainage04.maidex.data.PlayDetail
import io.github.brainage04.maidex.data.PlayerProfile
import io.github.brainage04.maidex.data.SongChart
import io.github.brainage04.maidex.data.SyncMedal
import io.github.brainage04.maidex.data.UserScore
import io.github.brainage04.maidex.data.normalizeSearch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.time.Instant
import java.net.URI
import java.time.YearMonth
import java.time.ZoneId
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class MaimaiDxClient(
    private val cookieManager: CookieManager = CookieManager.getInstance(),
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun syncAccount(region: AccountRegion): AccountSyncResult = withContext(Dispatchers.IO) {
        requireAuthenticatedCookie(region)
        val home = getDocument(region, "/maimai-mobile/home/")
        val baseProfile = extractPlayerProfile(home, region)
            ?: throw AuthenticationRequiredException("DX NET login expired; sign in again")
        loadAccountSnapshot(region, baseProfile)
    }

    suspend fun import(
        region: AccountRegion,
        charts: List<SongChart>,
        onProgress: (String) -> Unit,
    ): ImportResult = withContext(Dispatchers.IO) {
        requireAuthenticatedCookie(region)
        val lookup = ChartLookup(charts)
        onProgress("Checking DX NET login…")
        val home = getDocument(region, "/maimai-mobile/home/")
        val baseProfile = extractPlayerProfile(home, region)
            ?: throw AuthenticationRequiredException("DX NET login expired; sign in again")
        onProgress("Importing account and circle data…")
        val account = loadAccountSnapshot(region, baseProfile)
        val scoreImport = loadScores(region, lookup, onProgress)

        onProgress("Reading recent play history…")
        val recent = getDocument(region, "/maimai-mobile/record/")
        val summaries = parseRecentSummaries(recent, lookup).distinctBy(RecentSummary::chartKey)
        val details = loadRecentDetails(region, summaries, onProgress)

        ImportResult(
            profile = account.profile,
            scores = scoreImport.scores,
            playDetails = details,
            unmatchedCharts = scoreImport.unmatched,
            circle = account.circle,
        )
    }

    private fun requireAuthenticatedCookie(region: AccountRegion) {
        if (cookieManager.getCookie(region.loginUrl).isNullOrBlank()) {
            throw AuthenticationRequiredException("Sign in to DX NET before importing")
        }
    }

    private suspend fun loadAccountSnapshot(
        region: AccountRegion,
        baseProfile: PlayerProfile,
    ): AccountSyncResult = coroutineScope {
        val profile = async { loadAccountProgress(region, baseProfile) }
        val circle = async {
            optionalSupplement {
                extractCircleData(
                    documents = getCircleDocuments(region),
                    playerName = baseProfile.name,
                    importedAt = baseProfile.importedAt,
                )
            }
        }
        AccountSyncResult(profile.await(), circle.await())
    }

    private suspend fun loadAccountProgress(
        region: AccountRegion,
        profile: PlayerProfile,
    ): PlayerProfile = coroutineScope {
        val mapDocument = async {
            optionalSupplement { getDocument(region, "/maimai-mobile/map/") }
        }
        val matchingDocument = async {
            optionalSupplement { getDocument(region, "/maimai-mobile/friend/matching/") }
        }
        profile.copy(
            completedChihoNames = mapDocument.await()
                ?.let(::extractCompletedChihoNames)
                .orEmpty(),
            friendClass = matchingDocument.await()
                ?.let(::extractFriendClass)
                .orEmpty()
                .ifBlank { profile.friendClass },
        )
    }

    private suspend fun loadScores(
        region: AccountRegion,
        lookup: ChartLookup,
        onProgress: (String) -> Unit,
    ): ScoreImportResult = coroutineScope {
        val difficulties = listOf(
            "basic" to "0",
            "advanced" to "1",
            "expert" to "2",
            "master" to "3",
            "remaster" to "4",
            "utage" to "10",
        )
        val completed = AtomicInteger()
        val semaphore = Semaphore(MAX_CONCURRENT_DX_NET_REQUESTS)
        val rawScores = difficulties.map { (difficulty, queryValue) ->
            async {
                semaphore.withPermit {
                    val document = getDocument(
                        region,
                        "/maimai-mobile/record/musicGenre/search/?genre=99&diff=$queryValue",
                    )
                    onProgress(
                        "Imported score pages ${completed.incrementAndGet()}/${difficulties.size}…",
                    )
                    document.select(".w_450.m_15.p_r.f_0")
                        .mapNotNull { block -> parseScoreBlock(block, difficulty) }
                }
            }
        }.awaitAll().flatten()

        val scores = LinkedHashMap<String, UserScore>()
        var unmatched = 0
        rawScores.forEach { raw ->
            val chart = lookup.find(raw.title, raw.type, raw.difficulty, raw.level)
            if (chart == null) {
                unmatched += 1
            } else {
                val score = UserScore(
                    chartKey = chart.chartKey,
                    achievement = raw.achievement,
                    grade = Grade.fromAchievement(raw.achievement),
                    comboMedal = raw.comboMedal,
                    syncMedal = raw.syncMedal,
                    dxScore = raw.dxScore,
                    maxDxScore = raw.maxDxScore,
                )
                val existing = scores[chart.chartKey]
                if (existing == null || score.achievement >= existing.achievement) {
                    scores[chart.chartKey] = score
                }
            }
        }
        ScoreImportResult(scores.values.toList(), unmatched)
    }

    private suspend fun loadRecentDetails(
        region: AccountRegion,
        summaries: List<RecentSummary>,
        onProgress: (String) -> Unit,
    ): List<PlayDetail> = coroutineScope {
        val completed = AtomicInteger()
        val semaphore = Semaphore(MAX_CONCURRENT_DX_NET_REQUESTS)
        summaries.map { summary ->
            async {
                semaphore.withPermit {
                    val detail = runCatching {
                        getDocument(
                            region,
                            "/maimai-mobile/record/playlogDetail/?idx=" +
                                java.net.URLEncoder.encode(summary.id, "UTF-8"),
                        )
                    }.map { document -> parsePlayDetail(document, summary) }
                        .getOrNull()
                    onProgress(
                        "Imported recent details ${completed.incrementAndGet()}/${summaries.size}…",
                    )
                    detail
                }
            }
        }.awaitAll().filterNotNull()
    }

    private suspend inline fun <T> optionalSupplement(
        crossinline block: suspend () -> T,
    ): T? = try {
        block()
    } catch (failure: AuthenticationRequiredException) {
        throw failure
    } catch (_: Exception) {
        null
    }

    private suspend fun getCircleDocuments(region: AccountRegion): List<Document> {
        val main = getDocument(region, "/maimai-mobile/circle/")
        val linkedUrls = linkedSetOf<String>()
        main.select("a[href]").forEach { link ->
            link.absUrl("href").takeIf(String::isNotBlank)?.let(linkedUrls::add)
        }
        main.select("form[action]").forEach { form ->
            if (!form.attr("method").equals("post", ignoreCase = true)) {
                form.absUrl("action").takeIf(String::isNotBlank)?.let(linkedUrls::add)
            }
        }
        Regex("""location(?:\.href)?\s*=\s*['"]([^'"]+)['"]""")
            .findAll(main.html())
            .map { match -> URI(main.location()).resolve(match.groupValues[1]).toString() }
            .forEach(linkedUrls::add)

        val safeUrls = linkedUrls.asSequence()
            .filter { url -> url.startsWith("${region.baseUrl}/maimai-mobile/circle") }
            .filterNot {
                it.substringBefore('?').trimEnd('/') ==
                    main.location().substringBefore('?').trimEnd('/')
            }
            .filterNot(::isCircleMutationUrl)
            .distinct()
            .take(12)
            .toList()
        val semaphore = Semaphore(MAX_CONCURRENT_DX_NET_REQUESTS)
        val safePages = coroutineScope {
            safeUrls.map { url ->
                async {
                    semaphore.withPermit {
                        runCatching { getDocument(region, url) }.getOrNull()
                    }
                }
            }.awaitAll().filterNotNull()
        }
        return listOf(main) + safePages
    }

    private fun getDocument(region: AccountRegion, path: String): Document {
        val url = if (path.startsWith("http")) path else region.baseUrl + path
        val cookie = cookieManager.getCookie(url)
            ?: throw AuthenticationRequiredException("DX NET login expired; sign in again")
        val request = Request.Builder()
            .url(url)
            .header("Cookie", cookie)
            .header(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 15) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/134.0 Mobile Safari/537.36",
            )
            .header("Referer", region.loginUrl)
            .build()
        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("DX NET returned HTTP ${response.code}")
            val updatedCookies = response.headers.values("Set-Cookie")
            if (updatedCookies.isNotEmpty()) {
                synchronized(cookieManager) {
                    updatedCookies.forEach { value ->
                        cookieManager.setCookie(region.baseUrl, value)
                    }
                    cookieManager.flush()
                }
            }
            val finalUrl = response.request.url.toString()
            if (
                !finalUrl.startsWith(region.baseUrl) ||
                finalUrl.substringBefore('?').trimEnd('/') ==
                "${region.baseUrl}/maimai-mobile".trimEnd('/')
            ) {
                throw AuthenticationRequiredException("DX NET login expired; sign in again")
            }
            Jsoup.parse(response.body?.string().orEmpty(), finalUrl)
        }
    }


    private fun parseScoreBlock(block: Element, difficulty: String): RawScore? {
        val scoreBlocks = block.select(".music_score_block")
        val achievement = parseDouble(scoreBlocks.getOrNull(0)?.text())
        if (achievement <= 0.0) return null
        val title = block.selectFirst(".music_name_block")?.text()?.trim().orEmpty()
        if (title.isBlank()) return null
        val level = block.selectFirst(".music_lv_block")?.text()?.trim().orEmpty()
        val type = when (imageName(block.selectFirst(".music_kind_icon, .music_kind_icon_utage img"))) {
            "music_dx" -> "dx"
            "music_utage" -> "utage"
            else -> "std"
        }
        val iconNames = block.select("img[src*=/img/music_icon_]").map(::imageName)
        val combo = iconNames.firstNotNullOfOrNull(::comboMedal) ?: ComboMedal.NONE
        val sync = iconNames.firstNotNullOfOrNull(::syncMedal) ?: SyncMedal.NONE
        val dxPair = parsePair(scoreBlocks.getOrNull(1)?.text())
        return RawScore(
            title = title,
            type = type,
            difficulty = difficulty,
            level = level,
            achievement = achievement,
            comboMedal = combo,
            syncMedal = sync,
            dxScore = dxPair.first,
            maxDxScore = dxPair.second,
        )
    }

    private fun parseRecentSummaries(document: Document, lookup: ChartLookup): List<RecentSummary> =
        document.select("form[action*=playlogDetail]").mapNotNull { form ->
            val block = form.closest(".p_10.t_l.f_0.v_b") ?: return@mapNotNull null
            val titleElement = block.selectFirst(
                ".basic_block.m_5.m_t_17.m_r_60, .basic_block.m_5.p_5.p_l_10.f_13.break",
            ) ?: return@mapNotNull null
            val title = extractRecentTitle(titleElement)
            val difficulty = when (imageName(block.selectFirst(".playlog_diff"))) {
                "diff_advanced" -> "advanced"
                "diff_expert" -> "expert"
                "diff_master" -> "master"
                "diff_remaster" -> "remaster"
                "diff_utage" -> "utage"
                else -> "basic"
            }
            val type = when (imageName(block.selectFirst(".playlog_music_kind_icon, .playlog_music_kind_icon_utage img"))) {
                "music_dx" -> "dx"
                "music_utage" -> "utage"
                else -> "std"
            }
            val level = block.selectFirst(".playlog_level_icon")?.text().orEmpty()
            val chart = lookup.find(title, type, difficulty, level) ?: return@mapNotNull null
            val id = form.selectFirst("input[name=idx]")?.attr("value").orEmpty()
            if (id.isBlank()) return@mapNotNull null
            val subtitle = block.selectFirst(".sub_title")?.text().orEmpty()
            val playedAt = Regex("\\d{4}/\\d{2}/\\d{2}\\s+\\d{2}:\\d{2}")
                .find(subtitle)?.value.orEmpty()
            RecentSummary(id, chart.chartKey, playedAt)
        }

    private fun parsePlayDetail(document: Document, summary: RecentSummary): PlayDetail {
        val rows = document.select(".playlog_notes_detail tr").associateBy { row ->
            imageName(row.selectFirst("th img"))
        }
        fun counts(name: String): JudgeCounts {
            val values = rows[name]?.select("td")?.map { parseInt(it.text()) }.orEmpty()
            return JudgeCounts(
                criticalPerfect = values.getOrElse(0) { 0 },
                perfect = values.getOrElse(1) { 0 },
                great = values.getOrElse(2) { 0 },
                good = values.getOrElse(3) { 0 },
                miss = values.getOrElse(4) { 0 },
            )
        }
        val fastLate = document.select(".playlog_fl_block .w_96").associate { block ->
            imageName(block.selectFirst("img")) to parseInt(block.text())
        }
        return PlayDetail(
            chartKey = summary.chartKey,
            playedAt = summary.playedAt,
            achievement = parseDouble(document.selectFirst(".playlog_achievement_txt")?.text()),
            fast = fastLate["fast"] ?: 0,
            late = fastLate["late"] ?: 0,
            judgments = JudgmentTable(
                tap = counts("tap"),
                hold = counts("hold"),
                slide = counts("slide"),
                touch = counts("touch"),
                breakNotes = counts("break"),
            ),
        )
    }

    private companion object {
        const val MAX_CONCURRENT_DX_NET_REQUESTS = 3
    }
}
data class AccountSyncResult(
    val profile: PlayerProfile,
    val circle: CircleData?,
)

private data class ScoreImportResult(
    val scores: List<UserScore>,
    val unmatched: Int,
)

private fun isCircleMutationUrl(url: String): Boolean {
    val value = url.lowercase(Locale.ROOT)
    return listOf(
        "create",
        "edit",
        "save",
        "update",
        "join",
        "leave",
        "withdraw",
        "disband",
        "delete",
        "transfer",
        "recruit",
        "search",
    ).any(value::contains)
}

internal fun extractCircleData(
    documents: List<Document>,
    playerName: String,
    importedAt: Long = Instant.now().toEpochMilli(),
): CircleData? {
    if (documents.isEmpty()) return null
    val pageText = documents.joinToString(" ") { it.text().normalizedWhitespace() }
    if (Regex(
            """(?:currently\s+not\s+in\s+a\s+circle|not\s+participating\s+in\s+a\s+circle|サークルに所属していません)""",
            RegexOption.IGNORE_CASE,
        ).containsMatchIn(pageText)
    ) {
        return null
    }

    fun selectedText(selector: String): String = documents.asSequence()
        .flatMap { it.select(selector).asSequence() }
        .map { it.text().normalizedWhitespace() }
        .firstOrNull(String::isNotBlank)
        .orEmpty()

    val name = selectedText(
        ".circle_name_block, [class*=circle_name], [id*=circle_name]",
    ).stripLabels("Circle name", "サークル名").ifBlank {
        Regex(
            """(?:Circle\s*name|サークル名)\s*[:：]?\s*(.+?)(?=\s+(?:Circle\s*code|サークルコード|Leader|リーダー)\b|$)""",
            RegexOption.IGNORE_CASE,
        ).find(pageText)?.groupValues?.get(1)?.trim().orEmpty()
    }
    val code = selectedText(
        ".circle_code_block, [class*=circle_code], [id*=circle_code]",
    ).stripLabels("Circle code", "サークルコード").ifBlank {
        Regex(
            """(?:Circle\s*code|サークルコード)\s*[:：]?\s*([A-Z0-9]{4,20})""",
            RegexOption.IGNORE_CASE,
        ).find(pageText)?.groupValues?.get(1).orEmpty()
    }
    val totalPoints = pointsNear(documents, ".circle_totalpoint_header_for_index")
        ?: Regex(
            """(?:circle\s+(?:cumulative|total)\s+points?|サークル累計ポイント)[^0-9]{0,80}([\d,]+)\s*PT""",
            RegexOption.IGNORE_CASE,
        ).find(pageText)?.groupValues?.get(1).parseOptionalInt()
        ?: 0
    val regionalRank = integerNear(documents, ".circle_pointranking_header", """([\d,]+)\s*(?:st|nd|rd|th|位)?""")
        ?: Regex(
            """(?:current|regional|circle)\s+(?:point\s+)?ranking[^0-9]{0,80}([\d,]+)|現在のランキング[^0-9]{0,40}([\d,]+)""",
            RegexOption.IGNORE_CASE,
        ).find(pageText)?.groupValues?.drop(1)?.firstOrNull(String::isNotBlank).parseOptionalInt()
    val rankingLabel = selectedText(".circle_pointranking_header")
        .ifBlank { if (regionalRank == null) "" else "Current regional ranking" }
    val leader = selectedText("[class*=circle_leader], [id*=circle_leader]")
        .stripLabels("Leader", "リーダー")
        .ifBlank {
            Regex(
                """(?:Leader|リーダー)\s*[:：]?\s*(.+?)(?=\s+(?:Comment|Circle\s*comment|サークルコメント|Tags?|$))""",
                RegexOption.IGNORE_CASE,
            ).find(pageText)?.groupValues?.get(1)?.trim().orEmpty()
        }
    val comment = selectedText("[class*=circle_comment], [id*=circle_comment]")
        .stripLabels("Circle comment", "Comment", "サークルコメント")
    val tags = documents.asSequence()
        .flatMap { document ->
            document.select("[class*=circle_tag], [class*=circle_genre]").asSequence()
        }
        .map { it.text().normalizedWhitespace() }
        .filter { it.isNotBlank() && it.length <= 60 }
        .distinct()
        .toList()
    val circleClass = selectedText(".circle_class_table, [class*=circle_class]")
        .stripLabels("Circle class", "Class", "サークルクラス")
    val memberCount = Regex(
        """(?:members?|メンバー数)\s*[:：]?\s*(\d{1,3})""",
        RegexOption.IGNORE_CASE,
    ).find(pageText)?.groupValues?.get(1).parseOptionalInt()
    val daysUntilReset = Regex(
        """(?:reset[^0-9]{0,30}(?:in|after)?|リセットまで\s*あと?)\s*(\d{1,3})\s*(?:days?|日)""",
        RegexOption.IGNORE_CASE,
    ).find(pageText)?.groupValues?.get(1).parseOptionalInt()
    val nextRewardPoints = Regex(
        """(?:next\s+reward|次の報酬)[^0-9]{0,40}([\d,]+)\s*PT""",
        RegexOption.IGNORE_CASE,
    ).find(pageText)?.groupValues?.get(1).parseOptionalInt()
    val updatedAt = Regex("""20\d{2}[/-]\d{1,2}[/-]\d{1,2}(?:\s+\d{1,2}:\d{2})?""")
        .find(pageText)?.value.orEmpty()
    val resolvedName = name.ifBlank {
        if (code.isNotBlank() || totalPoints > 0 || regionalRank != null) "Your circle" else return null
    }

    val information = linkedMapOf<String, String>()
    documents.forEach { document ->
        document.select("tr").forEach { row ->
            val cells = row.children().filter { it.tagName() == "th" || it.tagName() == "td" }
            if (cells.size >= 2) {
                information.putIfUseful(cells.first().text(), cells.drop(1).joinToString(" ") { it.text() })
            }
        }
        document.select("dt").forEach { label ->
            information.putIfUseful(label.text(), label.nextElementSibling()?.text().orEmpty())
        }
        document.select(".circle_class_table").forEach { table ->
            table.children().chunked(2).forEach { pair ->
                if (pair.size == 2) information.putIfUseful(pair[0].text(), pair[1].text())
            }
        }
    }
    information.putIfUseful("Circle name", resolvedName)
    information.putIfUseful("Circle code", code)
    information.putIfUseful("Leader", leader)
    information.putIfUseful("Comment", comment)
    information.putIfUseful("Circle class", circleClass)
    information.putIfUseful("Total circle points", totalPoints.takeIf { it > 0 }?.toString().orEmpty())
    information.putIfUseful("Regional rank", regionalRank?.toString().orEmpty())
    information.putIfUseful("Members", memberCount?.toString().orEmpty())
    information.putIfUseful("Days until reset", daysUntilReset?.toString().orEmpty())
    information.putIfUseful("Points until next reward", nextRewardPoints?.toString().orEmpty())
    information.putIfUseful("Last updated by DX NET", updatedAt)

    val pages = documents
        .map(::extractCirclePageInfo)
        .filter { page -> page.text.isNotBlank() || page.items.isNotEmpty() || page.imageUrls.isNotEmpty() }
        .distinctBy { page -> page.type to page.title }

    val members = extractCircleMembers(documents, playerName)
    val rewards = extractCircleRewards(documents, totalPoints)
    return CircleData(
        month = circleMonth(pageText, importedAt),
        name = resolvedName,
        code = code,
        leader = leader,
        comment = comment,
        tags = tags,
        circleClass = circleClass,
        totalPoints = totalPoints,
        regionalRank = regionalRank,
        rankingLabel = rankingLabel,
        memberCount = memberCount ?: members.size.takeIf { members.isNotEmpty() },
        daysUntilReset = daysUntilReset,
        nextRewardPoints = nextRewardPoints,
        updatedAt = updatedAt,
        characterUrl = documents.firstNotNullOfOrNull { document ->
            document.selectFirst("[class*=circle] img[src*=Chara], [class*=circle] img[src*=chara]").imageUrl()
                .takeIf(String::isNotBlank)
        }.orEmpty(),
        backgroundUrl = documents.firstNotNullOfOrNull { document ->
            document.selectFirst("[class*=circle] img[src*=Background], [class*=circle] img[src*=background]").imageUrl()
                .takeIf(String::isNotBlank)
        }.orEmpty(),
        profileImageUrl = extractCircleProfileImage(documents),
        members = members,
        rewards = rewards,
        information = information.map { (label, value) -> CircleInfoItem(label, value) },
        pages = pages,
        importedAt = importedAt,
    )
}

private fun extractCircleMembers(documents: List<Document>, playerName: String): List<CircleMember> {
    val pointPattern = Regex("""([\d,]+)\s*(?:PT|points?)\b""", RegexOption.IGNORE_CASE)
    return documents.asSequence()
        .flatMap { document ->
            document.select("[class*=circle_member], [class*=member_block], [class*=member_row], tr").asSequence()
        }
        .mapNotNull { block ->
            val text = block.text().normalizedWhitespace()
            val pointMatch = pointPattern.find(text) ?: return@mapNotNull null
            if (Regex(
                    """circle\s+(?:total|cumulative)|ranking|reward|reset|サークル累計|ランキング|報酬""",
                    RegexOption.IGNORE_CASE,
                ).containsMatchIn(text)
            ) {
                return@mapNotNull null
            }
            val selectedName = block.selectFirst(
                "[class*=member_name], [class*=user_name], .name_block",
            )?.text()?.normalizedWhitespace().orEmpty()
            val rowName = block.children()
                .firstOrNull { child -> !pointPattern.containsMatchIn(child.text()) && child.text().isNotBlank() }
                ?.text()
                ?.normalizedWhitespace()
                .orEmpty()
            val name = selectedName.ifBlank { rowName }.ifBlank {
                text.substring(0, pointMatch.range.first)
                    .replace(Regex("""^\s*\d+\s*[.)位]?\s*"""), "")
                    .replace(Regex("""\b(?:leader|member|subleader)\b""", RegexOption.IGNORE_CASE), "")
                    .trim(' ', ':', '：', '-')
            }
            if (name.isBlank() || name.length > 80) return@mapNotNull null
            val role = Regex(
                """\b(leader|subleader|member)\b|(?:リーダー|サブリーダー|メンバー)""",
                RegexOption.IGNORE_CASE,
            ).find(text)?.value.orEmpty()
            val link = block.selectFirst("a[href*=idx], a[href*=user], a[href*=member]")?.absUrl("href").orEmpty()
            val key = Regex("""(?:idx|userId|memberId)=([^&#]+)""", RegexOption.IGNORE_CASE)
                .find(link)?.groupValues?.get(1)
                ?.takeIf(String::isNotBlank)
                ?: normalizeSearch(name)
            CircleMember(
                key = key,
                name = name,
                points = pointMatch.groupValues[1].parseOptionalInt() ?: 0,
                role = role,
                avatarUrl = block.selectFirst("img[src*=Icon], img[class*=icon]").imageUrl(),
                isCurrentUser = normalizeSearch(name) == normalizeSearch(playerName) ||
                    block.classNames().any { value -> value.contains("my", ignoreCase = true) },
            )
        }
        .distinctBy(CircleMember::key)
        .sortedWith(compareByDescending<CircleMember>(CircleMember::points).thenBy(CircleMember::name))
        .toList()
}

private fun extractCircleRewards(documents: List<Document>, totalPoints: Int): List<CircleReward> {
    val pointPattern = Regex("""([\d,]+)\s*PT\b""", RegexOption.IGNORE_CASE)
    return documents.asSequence()
        .filter { document ->
            Regex("""reward|報酬""", RegexOption.IGNORE_CASE).containsMatchIn(document.text())
        }
        .flatMap { document ->
            document.select("[class*=reward], .basic_block, tr").asSequence()
        }
        .mapNotNull { block ->
            val text = block.text().normalizedWhitespace()
            val pointMatch = pointPattern.find(text) ?: return@mapNotNull null
            if (Regex(
                    """circle\s+(?:total|cumulative)|next\s+reward|サークル累計|次の報酬""",
                    RegexOption.IGNORE_CASE,
                ).containsMatchIn(text)
            ) {
                return@mapNotNull null
            }
            val threshold = pointMatch.groupValues[1].parseOptionalInt() ?: return@mapNotNull null
            val image = block.selectFirst("img:not([src*=line]):not([src*=background])")
            val imageName = image?.attr("alt")?.normalizedWhitespace().orEmpty()
            val name = imageName.ifBlank {
                text.removeRange(pointMatch.range)
                    .replace(
                        Regex(
                            """(?:received|earned|acquired|達成|獲得|×\s*\d+|x\s*\d+)""",
                            RegexOption.IGNORE_CASE,
                        ),
                        "",
                    )
                    .trim(' ', ':', '：', '-', '·')
            }
            if (name.isBlank() || name.length > 160) return@mapNotNull null
            CircleReward(
                pointsRequired = threshold,
                name = name,
                imageUrl = image.imageUrl(),
                earned = threshold <= totalPoints || Regex(
                    """received|earned|acquired|達成|獲得""",
                    RegexOption.IGNORE_CASE,
                ).containsMatchIn(text),
            )
        }
        .distinctBy { reward -> reward.pointsRequired to reward.name }
        .sortedBy(CircleReward::pointsRequired)
        .toList()
}

private fun pointsNear(documents: List<Document>, selector: String): Int? =
    integerNear(documents, selector, """([\d,]+)\s*PT""")

private fun integerNear(documents: List<Document>, selector: String, pattern: String): Int? {
    val regex = Regex(pattern, RegexOption.IGNORE_CASE)
    return documents.asSequence()
        .flatMap { it.select(selector).asSequence() }
        .flatMap { element ->
            sequenceOf(element, element.parent(), element.parent()?.parent()).filterNotNull()
        }
        .mapNotNull { element -> regex.find(element.text())?.groupValues?.get(1).parseOptionalInt() }
        .firstOrNull()
}

private fun circleMonth(text: String, importedAt: Long): String {
    val current = YearMonth.from(Instant.ofEpochMilli(importedAt).atZone(ZoneId.systemDefault()))
    val numericMonth = Regex("""(\d{1,2})月度""").find(text)?.groupValues?.get(1)?.toIntOrNull()
    val englishMonth = MONTH_NAMES.indexOfFirst { month ->
        Regex("""\b$month\b""", RegexOption.IGNORE_CASE).containsMatchIn(text)
    }.takeIf { it >= 0 }?.plus(1)
    val month = numericMonth ?: englishMonth ?: current.monthValue
    val datedYear = Regex("""(20\d{2})[/-]\d{1,2}[/-]\d{1,2}""")
        .find(text)?.groupValues?.get(1)?.toIntOrNull()
    val year = datedYear ?: if (month > current.monthValue + 6) current.year - 1 else current.year
    return String.format(Locale.ROOT, "%04d-%02d", year, month)
}

private val MONTH_NAMES = listOf(
    "January",
    "February",
    "March",
    "April",
    "May",
    "June",
    "July",
    "August",
    "September",
    "October",
    "November",
    "December",
)

private fun circlePageTitle(document: Document): String {
    val type = circlePageType(document)
    return when (type) {
        CirclePageType.PROFILE -> "Circle profile"
        CirclePageType.INVITE_ACCEPT -> "Circle invitations"
        CirclePageType.FESTA -> "Circle Festa"
        CirclePageType.CHALLENGE_RANKING -> "Circle Challenge ranking"
        CirclePageType.POINT_REWARD -> "Circle point rewards"
        CirclePageType.RANKING -> "Circle ranking"
        CirclePageType.MEMBER -> "Circle members"
        CirclePageType.OTHER -> circlePageKey(document)
            .removePrefix("circle_")
            .split('_')
            .filter(String::isNotBlank)
            .joinToString(" ") { word ->
                word.replace(Regex("""([a-z])([A-Z])"""), "$1 $2")
                    .replaceFirstChar { character -> character.titlecase(Locale.ROOT) }
            }
            .ifBlank {
                document.title()
                    .replace("maimai DX NET", "", ignoreCase = true)
                    .trim(' ', '-', '－')
                    .ifBlank { "Circle" }
            }
    }
}

private fun circlePageType(document: Document): CirclePageType {
    val key = circlePageKey(document)
        .replace("_", "")
        .replace("-", "")
        .lowercase(Locale.ROOT)
    return when {
        "circlechallenge" in key && "ranking" in key -> CirclePageType.CHALLENGE_RANKING
        "inviteaccept" in key || "invitation" in key -> CirclePageType.INVITE_ACCEPT
        "pointreward" in key || ("reward" in key && "circle" in key) -> CirclePageType.POINT_REWARD
        "festa" in key -> CirclePageType.FESTA
        "profile" in key -> CirclePageType.PROFILE
        "member" in key -> CirclePageType.MEMBER
        "ranking" in key -> CirclePageType.RANKING
        else -> CirclePageType.OTHER
    }
}

private fun circlePageKey(document: Document): String {
    val titleImage = document.selectFirst("img.title")?.attr("src")
        ?.substringAfterLast('/')
        ?.substringBefore('.')
        ?.removePrefix("title_")
        .orEmpty()
    if (titleImage.isNotBlank()) return titleImage
    return runCatching { URI(document.location()).path }
        .getOrNull()
        ?.trim('/')
        ?.substringAfterLast('/')
        .orEmpty()
}

private fun extractCirclePageInfo(document: Document): CirclePageInfo {
    val content = (document.selectFirst(".main_wrapper") ?: document.body()).clone()
    content.select("script, style, header, footer, #page-top, #page-bottom").remove()
    val itemSelector = listOf(
        "tr",
        ".circle_member_block",
        ".circle_member_row",
        "[class*=ranking_block]",
        "[class*=rank_block]",
        "[class*=reward_block]",
        "[class*=circle_reward]",
        "[class*=festa_block]",
        "[class*=challenge_block]",
        ".basic_block",
        ".see_through_block",
    ).joinToString(", ")
    val items = content.select(itemSelector).mapNotNull { block ->
        val text = block.text().normalizedWhitespace()
        if (text.isBlank() || text.length > 500) return@mapNotNull null
        val imageUrl = block.selectFirst("img[src]").imageUrl()
            .takeIf(::isUsefulCirclePageImage)
            .orEmpty()
        val cells = if (block.tagName() == "tr") {
            block.children().filter { child -> child.tagName() == "th" || child.tagName() == "td" }
        } else {
            emptyList()
        }
        val label = if (cells.size >= 2) {
            cells.first().text().normalizedWhitespace()
        } else {
            block.selectFirst(
                "[class*=name], [class*=title], [class*=header], h1, h2, h3, strong, b",
            )?.text()?.normalizedWhitespace().orEmpty()
        }.ifBlank { text }
        val value = if (cells.size >= 2) {
            cells.drop(1).joinToString(" ") { cell -> cell.text().normalizedWhitespace() }
        } else {
            text.removePrefix(label).trim(' ', ':', '：', '-', '·')
        }
        CirclePageItem(label = label, value = value, imageUrl = imageUrl)
    }.distinctBy { item -> Triple(item.label, item.value, item.imageUrl) }

    val imageUrls = content.select("img[src]")
        .mapNotNull { image -> image.imageUrl().takeIf(::isUsefulCirclePageImage) }
        .distinct()
        .take(12)
    return CirclePageInfo(
        title = circlePageTitle(document),
        text = content.text().normalizedWhitespace(),
        type = circlePageType(document),
        items = items,
        imageUrls = imageUrls,
    )
}

private fun extractCircleProfileImage(documents: List<Document>): String =
    documents.asSequence().mapNotNull { document ->
        sequenceOf(
            document.selectFirst("[class*=circle_profile] img[src]"),
            document.selectFirst("[class*=circle] img[src*=Icon], [class*=circle] img[src*=icon]"),
            document.selectFirst("[class*=circle] img[src*=Profile], [class*=circle] img[src*=profile]"),
        ).map { image -> image.imageUrl() }
            .firstOrNull { url -> isUsefulCirclePageImage(url) }
    }.firstOrNull().orEmpty()

private fun isUsefulCirclePageImage(url: String): Boolean {
    if (url.isBlank()) return false
    val value = url.lowercase(Locale.ROOT)
    return listOf(
        "title_",
        "/header",
        "/footer",
        "line_",
        "/btn_",
        "/button_",
        "background",
    ).none(value::contains)
}

private fun MutableMap<String, String>.putIfUseful(label: String, value: String) {
    val cleanLabel = label.normalizedWhitespace().trim(' ', ':', '：')
    val cleanValue = value.normalizedWhitespace().trim()
    if (cleanLabel.isNotBlank() && cleanValue.isNotBlank() && cleanLabel.length <= 80 && cleanValue.length <= 500) {
        putIfAbsent(cleanLabel, cleanValue)
    }
}

private fun String.stripLabels(vararg labels: String): String {
    var value = normalizedWhitespace()
    labels.forEach { label ->
        value = value.replace(Regex("""^${Regex.escape(label)}\s*[:：]?\s*""", RegexOption.IGNORE_CASE), "")
    }
    return value.trim()
}

private fun String.normalizedWhitespace(): String = replace(Regex("""\s+"""), " ").trim()

private fun String?.parseOptionalInt(): Int? = this
    ?.replace(Regex("""[^0-9-]"""), "")
    ?.toIntOrNull()


internal fun extractCompletedChihoNames(document: Document): Set<String> =
    document.select(
        ".map_comp_img, .event_map_comp_img, img[src*=map_comp], img[src*=map_complete]",
    ).mapNotNullTo(linkedSetOf()) { completion ->
        generateSequence(completion.parent()) { element -> element.parent() }
            .take(7)
            .mapNotNull { container ->
                container.selectFirst(
                    ".map_name_block_inner, .map_name_block_s_inner, .mapdetail_name_block_inner",
                )?.text()?.normalizedWhitespace()?.takeIf(String::isNotBlank)
            }
            .firstOrNull()
    }

internal fun extractFriendClass(document: Document): String {
    val labeledClass = Regex(
        """(?:friend\s*class|class|クラス|階級)\s*[:：]?\s*(LEGEND|SSS[1-5]?|SS[1-5]?|S[1-5]?|A[1-5]?|B[1-5]?)""",
        RegexOption.IGNORE_CASE,
    ).find(document.text())?.groupValues?.getOrNull(1)
    if (!labeledClass.isNullOrBlank()) return labeledClass.uppercase(Locale.ROOT)
    val classRankUrl = document
        .selectFirst("""img[src*="/class/class_rank_"]""")
        .imageUrl()
    return friendClassFromUrl(classRankUrl)
}

private fun friendClassFromUrl(url: String): String = Regex(
    """class_rank_(legend|sss[1-5]?|ss[1-5]?|s[1-5]?|a[1-5]?|b[1-5]?)_""",
    RegexOption.IGNORE_CASE,
).find(url)?.groupValues?.getOrNull(1)?.uppercase(Locale.ROOT).orEmpty()
internal fun extractPlayerProfile(
    document: Document,
    region: AccountRegion,
    importedAt: Long = Instant.now().toEpochMilli(),
): PlayerProfile? {
    val block = document.selectFirst(".see_through_block") ?: return null
    val name = block.selectFirst(".name_block")?.text()?.trim().orEmpty()
    val ratingText = block.selectFirst(".rating_block")?.text().orEmpty()
    if (name.isBlank() && ratingText.isBlank()) return null

    val titleRarity = block.selectFirst(".trophy_block")
        ?.classNames()
        ?.firstOrNull { it.startsWith("trophy_") && it != "trophy_block" }
        ?.removePrefix("trophy_")
        .orEmpty()
    val rankImages = block.select("img.h_35.f_l")
    val courseRank = rankImages.firstOrNull { !it.hasClass("p_l_10") }
    val classRank = rankImages.firstOrNull { it.hasClass("p_l_10") }
        ?: rankImages.getOrNull(1)
    val starText = block.selectFirst(".p_l_10.f_l.f_14")?.text().orEmpty()
    val avatar = block.selectFirst("img.w_112.f_l")
        ?: block.selectFirst("""img[src*="/img/Icon/"]""")
    val pageText = document.text().normalizedWhitespace()
    val currentVersionPlayCount = playCountAfterLabels(
        pageText,
        "play count of current version",
        "current version play count",
        "今バージョンのプレイ回数",
        "今バージョンプレイ回数",
    )
    val totalPlayCount = playCountAfterLabels(
        pageText,
        "maimaiDX total play count",
        "total maimaiDX play count",
        "maimaiでらっくす総プレイ回数",
        "maimaiDX総プレイ回数",
    )

    return PlayerProfile(
        name = name.ifBlank { "Player" },
        officialRating = parseInt(ratingText),
        region = region,
        title = block.selectFirst(".trophy_inner_block")?.text()?.trim().orEmpty(),
        titleRarity = titleRarity,
        starCount = parseInt(starText).takeIf { starText.isNotBlank() },
        avatarUrl = avatar.imageUrl(),
        courseRankUrl = courseRank.imageUrl(),
        classRankUrl = classRank.imageUrl(),
        friendClass = friendClassFromUrl(classRank.imageUrl()),
        currentVersionPlayCount = currentVersionPlayCount,
        totalPlayCount = totalPlayCount,
        importedAt = importedAt,
    )
}

private fun playCountAfterLabels(text: String, vararg labels: String): Int? =
    labels.firstNotNullOfOrNull { label ->
        Regex(
            """${Regex.escape(label)}\s*[:：]?\s*([\d,]+)""",
            RegexOption.IGNORE_CASE,
        ).find(text)?.groupValues?.get(1).parseOptionalInt()
    }

private fun Element?.imageUrl(): String = this?.absUrl("src")
    ?.ifBlank { attr("src") }
    .orEmpty()


internal fun extractRecentTitle(titleElement: Element): String =
    titleElement.clone()
        .also { it.select("img, .playlog_level_icon").remove() }
        .text()
        .trim()


class AuthenticationRequiredException(message: String) : IllegalStateException(message)

private class ChartLookup(charts: List<SongChart>) {
    private val exact = charts.associateBy {
        matchKey(it.title, it.type, it.difficulty, it.level.orEmpty())
    }
    private val withoutLevel = charts.groupBy {
        matchKey(it.title, it.type, it.difficulty, "")
    }

    fun find(title: String, type: String, difficulty: String, level: String): SongChart? =
        exact[matchKey(title, type, difficulty, level)]
            ?: withoutLevel[matchKey(title, type, difficulty, "")]?.singleOrNull()
}

private fun matchKey(title: String, type: String, difficulty: String, level: String): String =
    listOf(normalizeSearch(title), type, difficulty, level.trim()).joinToString("\u001f")

private fun imageName(element: Element?): String = element?.attr("src")
    ?.substringAfterLast('/')
    ?.substringBefore('?')
    ?.removeSuffix(".png")
    .orEmpty()

private fun comboMedal(name: String): ComboMedal? = when (name) {
    "music_icon_fc", "fc" -> ComboMedal.FC
    "music_icon_fcp", "fcplus" -> ComboMedal.FC_PLUS
    "music_icon_ap", "ap" -> ComboMedal.AP
    "music_icon_app", "applus" -> ComboMedal.AP_PLUS
    else -> null
}

private fun syncMedal(name: String): SyncMedal? = when (name) {
    "music_icon_sync", "sync" -> SyncMedal.SYNC
    "music_icon_fs", "fs" -> SyncMedal.FS
    "music_icon_fsp", "fsplus" -> SyncMedal.FS_PLUS
    "music_icon_fsd", "fsd", "music_icon_fdx" -> SyncMedal.FDX
    "music_icon_fsdp", "fsdplus", "music_icon_fdxp" -> SyncMedal.FDX_PLUS
    else -> null
}

private fun parseInt(value: String?): Int = value.orEmpty()
    .replace(Regex("[^0-9-]"), "")
    .toIntOrNull() ?: 0

private fun parseDouble(value: String?): Double = value.orEmpty()
    .replace(",", "")
    .replace("%", "")
    .trim()
    .toDoubleOrNull() ?: 0.0

private fun parsePair(value: String?): Pair<Int, Int> {
    val values = value.orEmpty().removePrefix("DX SCORE").split('/')
    return parseInt(values.getOrNull(0)) to parseInt(values.getOrNull(1))
}

private fun difficultyLabel(value: String): String = when (value) {
    "basic" -> "BASIC"
    "advanced" -> "ADVANCED"
    "expert" -> "EXPERT"
    "master" -> "MASTER"
    "remaster" -> "Re:MASTER"
    "utage" -> "宴"
    else -> value
}

private data class RawScore(
    val title: String,
    val type: String,
    val difficulty: String,
    val level: String,
    val achievement: Double,
    val comboMedal: ComboMedal,
    val syncMedal: SyncMedal,
    val dxScore: Int,
    val maxDxScore: Int,
)

private data class RecentSummary(val id: String, val chartKey: String, val playedAt: String)
