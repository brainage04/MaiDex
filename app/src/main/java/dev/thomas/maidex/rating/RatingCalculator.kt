package dev.thomas.maidex.rating

import dev.thomas.maidex.data.ComboMedal
import dev.thomas.maidex.data.JudgeCounts
import dev.thomas.maidex.data.SongChart
import dev.thomas.maidex.data.UserScore
import kotlin.math.floor

object RatingCalculator {
    private val breakpoints = listOf(
        0.0 to 0.0,
        10.0 to 1.6,
        20.0 to 3.2,
        30.0 to 4.8,
        40.0 to 6.4,
        50.0 to 8.0,
        60.0 to 9.6,
        70.0 to 11.2,
        75.0 to 12.0,
        79.9999 to 12.8,
        80.0 to 13.6,
        90.0 to 15.2,
        94.0 to 16.8,
        96.9999 to 17.6,
        97.0 to 20.0,
        98.0 to 20.3,
        98.9999 to 20.6,
        99.0 to 20.8,
        99.5 to 21.1,
        99.9999 to 21.4,
        100.0 to 21.6,
        100.4999 to 22.2,
        100.5 to 22.4,
    )

    fun chartRating(constant: Double?, achievement: Double, isAllPerfect: Boolean): Int? {
        constant ?: return null
        val coefficient = breakpoints.last { achievement >= it.first }.second
        val base = floor(coefficient * constant * achievement.coerceAtMost(100.5) / 100.0).toInt()
        return base + if (isAllPerfect) 1 else 0
    }

    fun chartRating(chart: SongChart, score: UserScore): Int? = chartRating(
        constant = chart.constant,
        achievement = score.achievement,
        isAllPerfect = score.comboMedal >= ComboMedal.AP,
    )

    fun totalRating(
        charts: List<SongChart>,
        scores: Map<String, UserScore>,
        newVersions: Set<String>,
        override: RatingOverride? = null,
    ): Int {
        val entries = charts.asSequence().mapNotNull { chart ->
            if (chart.isSpecial) return@mapNotNull null
            val score = scores[chart.chartKey] ?: return@mapNotNull null
            val rating = if (override?.chartKey == chart.chartKey) {
                chartRating(chart.constant, override.achievement, override.isAllPerfect)
            } else {
                chartRating(chart, score)
            } ?: return@mapNotNull null
            RatingEntry(rating, chart.chartVersion in newVersions)
        }.toList()
        return entries.asSequence()
            .filter(RatingEntry::isNew)
            .map(RatingEntry::value)
            .sortedDescending()
            .take(15)
            .sum() + entries.asSequence()
            .filterNot(RatingEntry::isNew)
            .map(RatingEntry::value)
            .sortedDescending()
            .take(35)
            .sum()
    }

    fun milestones(
        chart: SongChart,
        score: UserScore,
        charts: List<SongChart>,
        scores: Map<String, UserScore>,
        newVersions: Set<String>,
        breakJudgments: JudgeCounts? = null,
    ): List<RatingMilestone> {
        val currentTotal = totalRating(charts, scores, newVersions)
        val rankTargets = listOf(
            "A" to 80.0,
            "AA" to 90.0,
            "AAA" to 94.0,
            "S" to 97.0,
            "S+" to 98.0,
            "SS" to 99.0,
            "SS+" to 99.5,
            "SSS" to 100.0,
            "SSS+" to 100.5,
        ).filter { (_, target) -> target > score.achievement }
            .map { (label, target) -> MilestoneTarget(label, target, target, false) }
        val medalTargets = buildList {
            if (score.comboMedal < ComboMedal.AP) {
                val breakCount = chart.noteCounts.breakNotes ?: 0
                val recordedPerfects = breakJudgments?.perfect?.coerceIn(0, breakCount)
                val label = breakJudgments?.let {
                    "AP (${it.criticalPerfect}/${it.perfect})"
                } ?: "AP"
                val minimum = when {
                    breakCount == 0 -> 100.0
                    recordedPerfects != null -> 101.0 - recordedPerfects * 0.5 / breakCount
                    else -> 100.5
                }
                val maximum = when {
                    breakCount == 0 -> 100.0
                    recordedPerfects != null -> 101.0 - recordedPerfects * 0.25 / breakCount
                    else -> 101.0
                }
                add(MilestoneTarget(label, minimum, maximum, true))
            }
        }
        return (rankTargets + medalTargets).distinctBy { it.label }.map { target ->
            val minimumChartRating = chartRating(
                chart.constant,
                target.minimumAchievement,
                target.isAllPerfect,
            )
            val maximumChartRating = chartRating(
                chart.constant,
                target.maximumAchievement,
                target.isAllPerfect,
            )
            val minimumTotal = totalRating(
                charts,
                scores,
                newVersions,
                RatingOverride(chart.chartKey, target.minimumAchievement, target.isAllPerfect),
            )
            val maximumTotal = totalRating(
                charts,
                scores,
                newVersions,
                RatingOverride(chart.chartKey, target.maximumAchievement, target.isAllPerfect),
            )
            RatingMilestone(
                label = target.label,
                minimumAchievement = target.minimumAchievement,
                maximumAchievement = target.maximumAchievement,
                minimumChartRating = minimumChartRating,
                maximumChartRating = maximumChartRating,
                minimumTotalChange = minimumTotal - currentTotal,
                maximumTotalChange = maximumTotal - currentTotal,
            )
        }
    }
}

data class RatingMilestone(
    val label: String,
    val minimumAchievement: Double,
    val maximumAchievement: Double,
    val minimumChartRating: Int?,
    val maximumChartRating: Int?,
    val minimumTotalChange: Int,
    val maximumTotalChange: Int,
)

data class RatingOverride(
    val chartKey: String,
    val achievement: Double,
    val isAllPerfect: Boolean,
)

private data class RatingEntry(val value: Int, val isNew: Boolean)
private data class MilestoneTarget(
    val label: String,
    val minimumAchievement: Double,
    val maximumAchievement: Double,
    val isAllPerfect: Boolean,
)
