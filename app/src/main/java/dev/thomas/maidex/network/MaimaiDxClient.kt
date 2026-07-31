package dev.thomas.maidex.network

import android.webkit.CookieManager
import dev.thomas.maidex.data.AccountRegion
import dev.thomas.maidex.data.ComboMedal
import dev.thomas.maidex.data.Grade
import dev.thomas.maidex.data.ImportResult
import dev.thomas.maidex.data.JudgeCounts
import dev.thomas.maidex.data.JudgmentTable
import dev.thomas.maidex.data.PlayDetail
import dev.thomas.maidex.data.PlayerProfile
import dev.thomas.maidex.data.SongChart
import dev.thomas.maidex.data.SyncMedal
import dev.thomas.maidex.data.UserScore
import dev.thomas.maidex.data.normalizeSearch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.time.Instant
import java.util.concurrent.TimeUnit

class MaimaiDxClient(
    private val cookieManager: CookieManager = CookieManager.getInstance(),
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun import(
        region: AccountRegion,
        charts: List<SongChart>,
        onProgress: (String) -> Unit,
    ): ImportResult = withContext(Dispatchers.IO) {
        if (cookieManager.getCookie(region.loginUrl).isNullOrBlank()) {
            throw AuthenticationRequiredException("Sign in to DX NET before importing")
        }
        val lookup = ChartLookup(charts)
        onProgress("Checking DX NET login…")
        val home = getDocument(region, "/maimai-mobile/home/")
        val profile = extractPlayerProfile(home, region)
            ?: throw AuthenticationRequiredException("DX NET login expired; sign in again")

        val scores = LinkedHashMap<String, UserScore>()
        var unmatched = 0
        val difficulties = listOf(
            "basic" to "0",
            "advanced" to "1",
            "expert" to "2",
            "master" to "3",
            "remaster" to "4",
            "utage" to "10",
        )
        difficulties.forEachIndexed { index, (difficulty, queryValue) ->
            onProgress("Importing ${difficultyLabel(difficulty)} scores (${index + 1}/${difficulties.size})…")
            val document = getDocument(
                region,
                "/maimai-mobile/record/musicGenre/search/?genre=99&diff=$queryValue",
            )
            document.select(".w_450.m_15.p_r.f_0").forEach { block ->
                val raw = parseScoreBlock(block, difficulty) ?: return@forEach
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
            if (index < difficulties.lastIndex) Thread.sleep(250)
        }

        onProgress("Reading recent play history…")
        val recent = getDocument(region, "/maimai-mobile/record/")
        val summaries = parseRecentSummaries(recent, lookup).distinctBy(RecentSummary::chartKey)
        val details = mutableListOf<PlayDetail>()
        summaries.forEachIndexed { index, summary ->
            onProgress("Importing recent details (${index + 1}/${summaries.size})…")
            runCatching {
                getDocument(
                    region,
                    "/maimai-mobile/record/playlogDetail/?idx=${java.net.URLEncoder.encode(summary.id, "UTF-8")}",
                )
            }.map { document -> parsePlayDetail(document, summary) }
                .getOrNull()
                ?.let(details::add)
            if (index < summaries.lastIndex) Thread.sleep(150)
        }

        ImportResult(
            profile = profile,
            scores = scores.values.toList(),
            playDetails = details,
            unmatchedCharts = unmatched,
        )
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
            response.headers.values("Set-Cookie").forEach { value ->
                cookieManager.setCookie(region.baseUrl, value)
            }
            cookieManager.flush()
            val finalUrl = response.request.url.toString()
            if (!finalUrl.startsWith(region.baseUrl)) {
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
}
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

    return PlayerProfile(
        name = name.ifBlank { "Player" },
        officialRating = parseInt(ratingText),
        region = region,
        title = block.selectFirst(".trophy_inner_block")?.text()?.trim().orEmpty(),
        titleRarity = titleRarity,
        starCount = parseInt(starText).takeIf { starText.isNotBlank() },
        avatarUrl = block.selectFirst("img.w_112.f_l").imageUrl(),
        courseRankUrl = courseRank.imageUrl(),
        classRankUrl = classRank.imageUrl(),
        importedAt = importedAt,
    )
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
