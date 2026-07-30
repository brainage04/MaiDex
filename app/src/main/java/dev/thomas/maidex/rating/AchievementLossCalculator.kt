package dev.thomas.maidex.rating

import dev.thomas.maidex.data.JudgeCounts
import dev.thomas.maidex.data.JudgmentTable
import dev.thomas.maidex.data.NoteCounts

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

data class LossRange(val minimum: Double, val maximum: Double)
