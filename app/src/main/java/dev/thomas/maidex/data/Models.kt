package dev.thomas.maidex.data

import java.text.Normalizer
import java.util.Locale

data class NoteCounts(
    val tap: Int?,
    val hold: Int?,
    val slide: Int?,
    val touch: Int?,
    val breakNotes: Int?,
    val total: Int?,
)

data class Regions(
    val jp: Boolean,
    val international: Boolean,
    val usa: Boolean,
    val china: Boolean,
) {
    fun codes(): Set<String> = buildSet {
        if (jp) add("jp")
        if (international) add("intl")
        if (usa) add("usa")
        if (china) add("cn")
    }
}

data class SongChart(
    val id: Long,
    val chartKey: String,
    val sourceSongId: String,
    val category: String,
    val title: String,
    val titleRomanized: String,
    val titleAliases: String,
    val artist: String,
    val artistRomanized: String,
    val bpm: Int?,
    val imageUrl: String,
    val songVersion: String,
    val releaseDate: String?,
    val comment: String?,
    val type: String,
    val difficulty: String,
    val level: String?,
    val levelValue: Double?,
    val constant: Double?,
    val noteDesigner: String?,
    val noteDesignerRomanized: String,
    val noteCounts: NoteCounts,
    val regions: Regions,
    val chartVersion: String?,
    val isSpecial: Boolean,
) {
    val searchableText: String = normalizeSearch(
        listOf(
            title,
            titleRomanized,
            titleAliases.replace('\u001e', ' '),
            artist,
            artistRomanized,
            noteDesigner.orEmpty(),
            noteDesignerRomanized,
        ).joinToString(" "),
    )

    val displayType: String
        get() = when (type) {
            "dx" -> "DX"
            "std" -> "STD"
            "utage" -> "宴"
            else -> type.uppercase(Locale.ROOT)
        }

    val displayDifficulty: String
        get() = when (difficulty) {
            "basic" -> "BASIC"
            "advanced" -> "ADVANCED"
            "expert" -> "EXPERT"
            "master" -> "MASTER"
            "remaster" -> "Re:MASTER"
            else -> difficulty.uppercase(Locale.ROOT)
        }

    val effectiveLevel: Double?
        get() = constant ?: levelValue
}

data class FilterOptions(
    val categories: List<String> = emptyList(),
    val versions: List<String> = emptyList(),
    val difficulties: List<String> = emptyList(),
    val types: List<String> = emptyList(),
    val regions: List<String> = listOf("jp", "intl", "usa", "cn"),
)

data class ChartFilters(
    val search: String = "",
    val artist: String = "",
    val noteDesigner: String = "",
    val categories: Set<String> = emptySet(),
    val difficulties: Set<String> = emptySet(),
    val versions: Set<String> = emptySet(),
    val types: Set<String> = emptySet(),
    val regions: Set<String> = emptySet(),
    val minLevel: Double? = null,
    val maxLevel: Double? = null,
    val minBpm: Int? = null,
    val maxBpm: Int? = null,
    val knownConstantsOnly: Boolean = false,
    val grades: Set<Grade> = emptySet(),
    val comboMedals: Set<ComboMedal> = emptySet(),
    val syncMedals: Set<SyncMedal> = emptySet(),
    val scoredOnly: Boolean = false,
) {
    val activeCount: Int
        get() = listOf(
            artist.isNotBlank(),
            noteDesigner.isNotBlank(),
            categories.isNotEmpty(),
            difficulties.isNotEmpty(),
            versions.isNotEmpty(),
            types.isNotEmpty(),
            regions.isNotEmpty(),
            minLevel != null,
            maxLevel != null,
            minBpm != null,
            maxBpm != null,
            knownConstantsOnly,
            grades.isNotEmpty(),
            comboMedals.isNotEmpty(),
            syncMedals.isNotEmpty(),
            scoredOnly,
        ).count { it }
}

enum class ChartSort(val label: String) {
    CONSTANT_DESC("Constant"),
    LEVEL_ASC("Level"),
    TITLE_ASC("Title"),
    RATING_DESC("Rating"),
    ACHIEVEMENT_DESC("Achievement"),
    GRADE_DESC("Rank"),
    RELEASE_DESC("Release"),
    BPM_ASC("BPM"),
}

data class CatalogInfo(
    val updateTime: String,
    val songCount: Int,
    val chartCount: Int,
    val sourceUrl: String,
)

internal fun normalizeSearch(value: String): String =
    Normalizer.normalize(value, Normalizer.Form.NFKC)
        .lowercase(Locale.ROOT)
        .replace(Regex("[\\p{Punct}\\p{Z}]+"), "")
