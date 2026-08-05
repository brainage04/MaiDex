package io.github.brainage04.maidex.rating

import io.github.brainage04.maidex.data.JudgeCounts
import io.github.brainage04.maidex.data.JudgmentTable
import io.github.brainage04.maidex.data.NoteCounts
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementLossCalculatorTest {
    private val cryogenic = NoteCounts(
        tap = 439,
        hold = 84,
        slide = 84,
        touch = 69,
        breakNotes = 64,
        total = 740,
    )

    @Test
    fun `non-break perfect loses dx score but no achievement`() {
        val perfect = AchievementLossCalculator.perJudgment(cryogenic)
            .single { it.noteType == "Tap" && it.judgment == "Perfect" }
        assertEquals(0.0, perfect.achievementLoss, 0.0)
        assertEquals(1, perfect.dxScoreLoss)
    }

    @Test
    fun `far break perfect loses more than near break perfect`() {
        val losses = AchievementLossCalculator.perJudgment(cryogenic)
        val near = losses.single { it.noteType == "Break" && it.judgment == "Perfect · near" }
        val far = losses.single { it.noteType == "Break" && it.judgment == "Perfect · far" }
        assertTrue(far.achievementLoss > near.achievementLoss)
    }

    @Test
    fun `aggregated break judgments produce bounded loss range`() {
        val range = AchievementLossCalculator.actualLossRange(
            cryogenic,
            JudgmentTable(
                tap = JudgeCounts(great = 2),
                breakNotes = JudgeCounts(perfect = 3, great = 1),
            ),
        )
        assertTrue(range.minimum > 0.0)
        assertTrue(range.maximum > range.minimum)
    }
    @Test
    fun `grouped judgment columns show aggregate loss for each count`() {
        val daredevil = NoteCounts(
            tap = 690,
            hold = 69,
            slide = 68,
            touch = 78,
            breakNotes = 99,
            total = 1_004,
        )
        val losses = AchievementLossCalculator.groupedJudgmentLosses(
            daredevil,
            JudgmentTable(
                tap = JudgeCounts(great = 10, good = 2, miss = 3),
                slide = JudgeCounts(great = 1),
                breakNotes = JudgeCounts(great = 1),
            ),
        ).associateBy { it.noteType }

        assertEquals(0.1246105919, losses.getValue("Tap").great.minimum, 0.0000000001)
        assertEquals(0.0683659020, losses.getValue("Break").great.minimum, 0.0000000001)
        assertEquals(0.1618238459, losses.getValue("Break").great.maximum, 0.0000000001)
    }

    @Test
    fun `achievement resolves the aggregate Break Great timing loss when unique`() {
        val daredevil = NoteCounts(690, 69, 68, 78, 99, 1_004)
        val judgments = JudgmentTable(
            tap = JudgeCounts(criticalPerfect = 493, perfect = 182, great = 10, good = 2, miss = 3),
            hold = JudgeCounts(criticalPerfect = 58, perfect = 11),
            slide = JudgeCounts(criticalPerfect = 67, great = 1),
            touch = JudgeCounts(criticalPerfect = 78),
            breakNotes = JudgeCounts(criticalPerfect = 86, perfect = 12, great = 1),
        )

        val loss = AchievementLossCalculator
            .groupedJudgmentLosses(daredevil, judgments, achievement = 100.3941)
            .single { it.noteType == "Break" }
            .great

        assertEquals(0.1618238459, loss.minimum, 0.0000000001)
        assertEquals(loss.minimum, loss.maximum, 0.0)
    }

}
