package io.github.brainage04.maidex.ui

import io.github.brainage04.maidex.data.AccountRegion
import io.github.brainage04.maidex.data.ClassBattleMetadata
import io.github.brainage04.maidex.data.DanCourse
import io.github.brainage04.maidex.data.DanCourseMetadata
import io.github.brainage04.maidex.data.SongChart
import io.github.brainage04.maidex.data.UnlockGuideEntry
import io.github.brainage04.maidex.data.UserScore
import io.github.brainage04.maidex.data.normalizeSearch

/**
 * Chiho and Class Battle entries whose title, romanised title, unlock songs (original or romanised)
 * or Class Battle boss songs contain [query]. Matching uses the catalog's [normalizeSearch], so case,
 * width and punctuation are ignored the same way as in the catalog search.
 */
internal fun filterUnlockGuideEntries(
    entries: List<UnlockGuideEntry>,
    query: String,
    romanizedTitles: Map<String, String>,
): List<UnlockGuideEntry> {
    val normalized = normalizeSearch(query)
    if (normalized.isEmpty()) return entries
    return entries.filter { entry ->
        val milestones = ClassBattleMetadata.byVersion[entry.title].orEmpty()
        val haystack = buildList {
            add(entry.title)
            add(entry.titleRomanized)
            entry.songs.forEach { song ->
                add(song.title)
                add(romanizedTitles[song.title].orEmpty())
            }
            milestones.forEach { milestone ->
                add(milestone.songTitle)
                add(romanizedTitles[milestone.songTitle].orEmpty())
            }
        }
        haystack.any { normalizeSearch(it).contains(normalized) }
    }
}

/**
 * Dan courses (already limited to one version) whose name, English name or tracks for [region]
 * contain [query]. Track titles are matched in their original and romanised forms.
 */
internal fun filterDanCourses(
    courses: List<DanCourse>,
    query: String,
    region: AccountRegion,
    chartLookup: Map<String, SongChart>,
): List<DanCourse> {
    val normalized = normalizeSearch(query)
    if (normalized.isEmpty()) return courses
    return courses.filter { course ->
        val haystack = buildList {
            add(course.name)
            add(course.nameEnglish)
            course.tracks.indices.forEach { index ->
                val track = course.track(index, region)
                add(track.title)
                add(chartLookup[DanCourseMetadata.chartKey(track)]?.titleRomanized.orEmpty())
            }
        }
        haystack.any { normalizeSearch(it).contains(normalized) }
    }
}

/**
 * The chart a guide song row opens: the player's best-scored chart of that song, otherwise its
 * MASTER chart (DX before standard), otherwise its hardest chart. Null when the catalog lacks the song.
 */
internal fun guideSongChart(
    songTitle: String,
    charts: List<SongChart>,
    scores: Map<String, UserScore>,
    level: String? = null,
): SongChart? {
    val songCharts = charts.filter { it.sourceSongId == songTitle || it.title == songTitle }
    if (songCharts.isEmpty()) return null
    if (level != null) {
        songCharts
            .filter { it.level == level }
            .maxWithOrNull(compareBy({ scores[it.chartKey]?.achievement ?: -1.0 }, { it.type == "dx" }))
            ?.let { return it }
    }
    songCharts
        .filter { scores[it.chartKey] != null }
        .maxByOrNull { scores.getValue(it.chartKey).achievement }
        ?.let { return it }
    return songCharts
        .filter { it.difficulty == "master" }
        .maxByOrNull { if (it.type == "dx") 1 else 0 }
        ?: songCharts.maxByOrNull { it.levelValue ?: 0.0 }
}
