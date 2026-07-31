package dev.thomas.maidex.data

import java.time.Instant

enum class AccountRegion(val label: String, val baseUrl: String) {
    JAPAN("Japan", "https://maimaidx.jp"),
    INTERNATIONAL("International", "https://maimaidx-eng.com"),
    ;

    val loginUrl: String get() = "$baseUrl/maimai-mobile/"
}

enum class Grade(val label: String, val threshold: Double) {
    SSS_PLUS("SSS+", 100.5),
    SSS("SSS", 100.0),
    SS_PLUS("SS+", 99.5),
    SS("SS", 99.0),
    S_PLUS("S+", 98.0),
    S("S", 97.0),
    AAA("AAA", 94.0),
    AA("AA", 90.0),
    A("A", 80.0),
    BBB("BBB", 75.0),
    BB("BB", 70.0),
    B("B", 60.0),
    C("C", 50.0),
    D("D", 0.0),
    ;

    companion object {
        fun fromAchievement(achievement: Double): Grade =
            entries.first { achievement >= it.threshold }
    }
}

enum class ComboMedal(val label: String) {
    NONE("—"),
    FC("FC"),
    FC_PLUS("FC+"),
    AP("AP"),
    AP_PLUS("AP+"),
}

enum class SyncMedal(val label: String) {
    NONE("—"),
    SYNC("SYNC"),
    FS("FS"),
    FS_PLUS("FS+"),
    FDX("FDX"),
    FDX_PLUS("FDX+"),
}

data class UserScore(
    val chartKey: String,
    val achievement: Double,
    val grade: Grade,
    val comboMedal: ComboMedal,
    val syncMedal: SyncMedal,
    val dxScore: Int,
    val maxDxScore: Int,
    val importedAt: Long = Instant.now().toEpochMilli(),
) {
    val dxScorePercentage: Double?
        get() = maxDxScore.takeIf { it > 0 }?.let { dxScore.toDouble() / it * 100.0 }

    val dxStarCount: Int
        get() {
            val percentage = dxScorePercentage ?: return 0
            return when (percentage) {
                in 97.0..Double.POSITIVE_INFINITY -> 5
                in 95.0..<97.0 -> 4
                in 93.0..<95.0 -> 3
                in 90.0..<93.0 -> 2
                in 85.0..<90.0 -> 1
                else -> 0
            }
        }
}

data class JudgeCounts(
    val criticalPerfect: Int = 0,
    val perfect: Int = 0,
    val great: Int = 0,
    val good: Int = 0,
    val miss: Int = 0,
)

data class JudgmentTable(
    val tap: JudgeCounts = JudgeCounts(),
    val hold: JudgeCounts = JudgeCounts(),
    val slide: JudgeCounts = JudgeCounts(),
    val touch: JudgeCounts = JudgeCounts(),
    val breakNotes: JudgeCounts = JudgeCounts(),
)

data class PlayDetail(
    val chartKey: String,
    val playedAt: String,
    val achievement: Double,
    val fast: Int,
    val late: Int,
    val judgments: JudgmentTable,
)

data class PlayerProfile(
    val name: String,
    val officialRating: Int,
    val region: AccountRegion,
    val title: String = "",
    val titleRarity: String = "",
    val starCount: Int? = null,
    val avatarUrl: String = "",
    val courseRankUrl: String = "",
    val classRankUrl: String = "",
    val titleBackgroundUrl: String = "",
    val ratingBaseUrl: String = "",
    val starIconUrl: String = "",
    val importedAt: Long = Instant.now().toEpochMilli(),
)

data class ImportResult(
    val profile: PlayerProfile,
    val scores: List<UserScore>,
    val playDetails: List<PlayDetail>,
    val unmatchedCharts: Int,
)
