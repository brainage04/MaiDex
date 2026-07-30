package dev.thomas.maidex.rating

import dev.thomas.maidex.data.JudgeCounts
import dev.thomas.maidex.data.JudgmentTable
import dev.thomas.maidex.data.NoteCounts
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
}
