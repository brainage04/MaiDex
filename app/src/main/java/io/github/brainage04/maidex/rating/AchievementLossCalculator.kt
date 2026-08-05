package io.github.brainage04.maidex.rating

import io.github.brainage04.maidex.data.JudgeCounts
import io.github.brainage04.maidex.data.JudgmentTable
import io.github.brainage04.maidex.data.NoteCounts

object AchievementLossCalculator {
    fun perJudgment(noteCounts: NoteCounts): List<JudgmentLoss> {
        val maximumBase = maximumBase(noteCounts)
        if (maximumBase <= 0.0) return emptyList()
        val rows = mutableListOf<JudgmentLoss>()
        addStandardRows(rows, "Tap", 500.0, maximumBase)
        addStandardRows(rows, "Hold", 1_000.0, maximumBase)
        addStandardRows(rows, "Slide", 1_500.0, maximumBase)
        if ((noteCounts.touch ?: 0) > 0) addStandardRows(rows, "Touch", 500.0, maximumBase)

        val breaks = noteCounts.breakNotes ?: 0
        if (breaks > 0) {
            rows += breakLoss("Perfect · near", baseLoss = 0.0, bonusLoss = 25.0, maximumBase, breaks, 1)
            rows += breakLoss("Perfect · far", baseLoss = 0.0, bonusLoss = 50.0, maximumBase, breaks, 1)
            rows += breakLoss("Great · high", baseLoss = 500.0, bonusLoss = 60.0, maximumBase, breaks, 2)
            rows += breakLoss("Great · mid", baseLoss = 1_000.0, bonusLoss = 60.0, maximumBase, breaks, 2)
            rows += breakLoss("Great · low", baseLoss = 1_250.0, bonusLoss = 60.0, maximumBase, breaks, 2)
            rows += breakLoss("Good", baseLoss = 1_500.0, bonusLoss = 70.0, maximumBase, breaks, 3)
            rows += breakLoss("Miss", baseLoss = 2_500.0, bonusLoss = 100.0, maximumBase, breaks, 3)
        }
        return rows
    }

    fun actualLossRange(noteCounts: NoteCounts, judgments: JudgmentTable): LossRange {
        val maximumBase = maximumBase(noteCounts)
        if (maximumBase <= 0.0) return LossRange(0.0, 0.0)
        var fixed = 0.0
        fixed += standardActual(judgments.tap, 500.0, maximumBase)
        fixed += standardActual(judgments.hold, 1_000.0, maximumBase)
        fixed += standardActual(judgments.slide, 1_500.0, maximumBase)
        fixed += standardActual(judgments.touch, 500.0, maximumBase)

        val breaks = noteCounts.breakNotes ?: 0
        if (breaks <= 0) return LossRange(fixed, fixed)
        val breakJudgments = judgments.breakNotes
        val nearPerfect = breakLossValue(0.0, 25.0, maximumBase, breaks)
        val farPerfect = breakLossValue(0.0, 50.0, maximumBase, breaks)
        val highGreat = breakLossValue(500.0, 60.0, maximumBase, breaks)
        val lowGreat = breakLossValue(1_250.0, 60.0, maximumBase, breaks)
        val good = breakLossValue(1_500.0, 70.0, maximumBase, breaks)
        val miss = breakLossValue(2_500.0, 100.0, maximumBase, breaks)
        val fixedBreak = breakJudgments.good * good + breakJudgments.miss * miss
        return LossRange(
            minimum = fixed + fixedBreak + breakJudgments.perfect * nearPerfect + breakJudgments.great * highGreat,
            maximum = fixed + fixedBreak + breakJudgments.perfect * farPerfect + breakJudgments.great * lowGreat,
        )
    }

    fun groupedJudgmentLosses(
        noteCounts: NoteCounts,
        judgments: JudgmentTable,
        achievement: Double? = null,
    ): List<JudgmentRowLoss> {
        val maximumBase = maximumBase(noteCounts)
        if (maximumBase <= 0.0) return emptyList()
        val rows = mutableListOf(
            standardGroupedLoss("Tap", judgments.tap, 500.0, maximumBase),
            standardGroupedLoss("Hold", judgments.hold, 1_000.0, maximumBase),
            standardGroupedLoss("Slide", judgments.slide, 1_500.0, maximumBase),
            standardGroupedLoss("Touch", judgments.touch, 500.0, maximumBase),
        )
        val breaks = noteCounts.breakNotes ?: 0
        if (breaks > 0) {
            val counts = judgments.breakNotes
            val greatRange = inferBreakGreatLoss(
                judgments = judgments,
                achievement = achievement,
                maximumBase = maximumBase,
                breaks = breaks,
            ) ?: LossRange(
                minimum = counts.great * breakLossValue(500.0, 60.0, maximumBase, breaks),
                maximum = counts.great * breakLossValue(1_250.0, 60.0, maximumBase, breaks),
            )
            rows += JudgmentRowLoss(
                noteType = "Break",
                great = greatRange,
                good = fixedRange(counts.good * breakLossValue(1_500.0, 70.0, maximumBase, breaks)),
                miss = fixedRange(counts.miss * breakLossValue(2_500.0, 100.0, maximumBase, breaks)),
            )
        }
        return rows
    }

    private fun inferBreakGreatLoss(
        judgments: JudgmentTable,
        achievement: Double?,
        maximumBase: Double,
        breaks: Int,
    ): LossRange? {
        achievement ?: return null
        val counts = judgments.breakNotes
        if (counts.great == 0) return fixedRange(0.0)
        val combinations =
            (counts.perfect + 1L) * (counts.great + 1L) * (counts.great + 2L) / 2L
        if (combinations > 1_000_000L) return null

        val knownLoss =
            standardActual(judgments.tap, 500.0, maximumBase) +
                standardActual(judgments.hold, 1_000.0, maximumBase) +
                standardActual(judgments.slide, 1_500.0, maximumBase) +
                standardActual(judgments.touch, 500.0, maximumBase) +
                counts.good * breakLossValue(1_500.0, 70.0, maximumBase, breaks) +
                counts.miss * breakLossValue(2_500.0, 100.0, maximumBase, breaks)
        val unknownLoss = 101.0 - achievement - knownLoss
        val nearPerfect = breakLossValue(0.0, 25.0, maximumBase, breaks)
        val farPerfect = breakLossValue(0.0, 50.0, maximumBase, breaks)
        val highGreat = breakLossValue(500.0, 60.0, maximumBase, breaks)
        val midGreat = breakLossValue(1_000.0, 60.0, maximumBase, breaks)
        val lowGreat = breakLossValue(1_250.0, 60.0, maximumBase, breaks)
        var minimum = Double.POSITIVE_INFINITY
        var maximum = Double.NEGATIVE_INFINITY

        for (farPerfectCount in 0..counts.perfect) {
            val perfectLoss =
                farPerfectCount * farPerfect + (counts.perfect - farPerfectCount) * nearPerfect
            for (midGreatCount in 0..counts.great) {
                for (lowGreatCount in 0..(counts.great - midGreatCount)) {
                    val highGreatCount = counts.great - midGreatCount - lowGreatCount
                    val greatLoss =
                        highGreatCount * highGreat + midGreatCount * midGreat + lowGreatCount * lowGreat
                    if (kotlin.math.abs(perfectLoss + greatLoss - unknownLoss) <= 0.000051) {
                        minimum = minOf(minimum, greatLoss)
                        maximum = maxOf(maximum, greatLoss)
                    }
                }
            }
        }
        return if (minimum.isFinite()) LossRange(minimum, maximum) else null
    }

    private fun standardGroupedLoss(
        noteType: String,
        judgments: JudgeCounts,
        weight: Double,
        maximumBase: Double,
    ): JudgmentRowLoss = JudgmentRowLoss(
        noteType = noteType,
        great = fixedRange(judgments.great * weight * 0.2 / maximumBase * 100.0),
        good = fixedRange(judgments.good * weight * 0.5 / maximumBase * 100.0),
        miss = fixedRange(judgments.miss * weight / maximumBase * 100.0),
    )

    private fun fixedRange(value: Double) = LossRange(value, value)

    private fun addStandardRows(
        rows: MutableList<JudgmentLoss>,
        noteType: String,
        weight: Double,
        maximumBase: Double,
    ) {
        rows += JudgmentLoss(noteType, "Perfect", 0.0, 1)
        rows += JudgmentLoss(noteType, "Great", weight * 0.2 / maximumBase * 100.0, 2)
        rows += JudgmentLoss(noteType, "Good", weight * 0.5 / maximumBase * 100.0, 3)
        rows += JudgmentLoss(noteType, "Miss", weight / maximumBase * 100.0, 3)
    }

    private fun standardActual(judgments: JudgeCounts, weight: Double, maximumBase: Double): Double =
        (judgments.great * weight * 0.2 + judgments.good * weight * 0.5 + judgments.miss * weight) /
            maximumBase * 100.0

    private fun breakLoss(
        judgment: String,
        baseLoss: Double,
        bonusLoss: Double,
        maximumBase: Double,
        breaks: Int,
        dxScoreLoss: Int,
    ) = JudgmentLoss(
        noteType = "Break",
        judgment = judgment,
        achievementLoss = breakLossValue(baseLoss, bonusLoss, maximumBase, breaks),
        dxScoreLoss = dxScoreLoss,
    )

    private fun breakLossValue(
        baseLoss: Double,
        bonusLoss: Double,
        maximumBase: Double,
        breaks: Int,
    ): Double = baseLoss / maximumBase * 100.0 + bonusLoss / (breaks * 100.0)

    private fun maximumBase(counts: NoteCounts): Double =
        (counts.tap ?: 0) * 500.0 +
            (counts.hold ?: 0) * 1_000.0 +
            (counts.slide ?: 0) * 1_500.0 +
            (counts.touch ?: 0) * 500.0 +
            (counts.breakNotes ?: 0) * 2_500.0
}

data class JudgmentLoss(
    val noteType: String,
    val judgment: String,
    val achievementLoss: Double,
    val dxScoreLoss: Int,
)

data class JudgmentRowLoss(
    val noteType: String,
    val great: LossRange,
    val good: LossRange,
    val miss: LossRange,
)

data class LossRange(val minimum: Double, val maximum: Double)
