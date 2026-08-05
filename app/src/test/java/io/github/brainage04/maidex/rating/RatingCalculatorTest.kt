package io.github.brainage04.maidex.rating
import io.github.brainage04.maidex.data.ComboMedal
import io.github.brainage04.maidex.data.Grade
import io.github.brainage04.maidex.data.JudgeCounts
import io.github.brainage04.maidex.data.NoteCounts
import io.github.brainage04.maidex.data.Regions
import io.github.brainage04.maidex.data.SongChart
import io.github.brainage04.maidex.data.SyncMedal
import io.github.brainage04.maidex.data.UserScore

import org.junit.Assert.assertEquals
import org.junit.Test

class RatingCalculatorTest {
    @Test
    fun `all perfect bonus is added after achievement cap`() {
        assertEquals(311, RatingCalculator.chartRating(13.8, 100.6123, true))
        assertEquals(
            RatingCalculator.chartRating(13.8, 100.5, true),
            RatingCalculator.chartRating(13.8, 101.0, true),
        )
    }

    @Test
    fun `upper boundary coefficient applies only at 99 point 9999`() {
        assertEquals(291, RatingCalculator.chartRating(13.8, 99.9998, false))
        assertEquals(295, RatingCalculator.chartRating(13.8, 99.9999, false))
    }

    @Test
    fun `all perfect adds exactly one rating`() {
        val ordinary = RatingCalculator.chartRating(14.0, 100.5, false)
        val allPerfect = RatingCalculator.chartRating(14.0, 100.5, true)
        assertEquals(ordinary!! + 1, allPerfect)
    }
    @Test
    fun `AP milestone uses best play break split and never offers AP plus`() {
        val chart = SongChart(
            id = 1,
            chartKey = "daredevil\u001fdx\u001fmaster",
            sourceSongId = "Daredevil Glaive",
            category = "GAME",
            title = "Daredevil Glaive",
            titleRomanized = "Daredevil Glaive",
            titleAliases = "",
            artist = "Artifact",
            artistRomanized = "Artifact",
            bpm = 185,
            imageUrl = "",
            songVersion = "CiRCLE",
            releaseDate = "2026-07-10",
            comment = null,
            type = "dx",
            difficulty = "master",
            level = "13+",
            levelValue = 13.7,
            constant = 13.8,
            noteDesigner = "柏木 咲姫",
            noteDesignerRomanized = "",
            noteCounts = NoteCounts(690, 69, 68, 78, 99, 1_004),
            regions = Regions(jp = true, international = true, usa = false, china = false),
            chartVersion = "CiRCLE",
            isSpecial = false,
        )
        val score = UserScore(
            chartKey = chart.chartKey,
            achievement = 100.4385,
            grade = Grade.SSS,
            comboMedal = ComboMedal.NONE,
            syncMedal = SyncMedal.NONE,
            dxScore = 0,
            maxDxScore = 0,
        )

        val milestones = RatingCalculator.milestones(
            chart = chart,
            score = score,
            charts = listOf(chart),
            scores = mapOf(chart.chartKey to score),
            newVersions = emptySet(),
            breakJudgments = JudgeCounts(criticalPerfect = 86, perfect = 12, great = 1),
        )
        val ap = milestones.single { it.label.startsWith("AP") }

        assertEquals("AP (86/12)", ap.label)
        assertEquals(100.9393939394, ap.minimumAchievement, 0.0000000001)
        assertEquals(100.9696969697, ap.maximumAchievement, 0.0000000001)
        assertEquals(false, milestones.any { it.label == "AP+" })
    }

}
