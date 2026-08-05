package io.github.brainage04.maidex.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ScoreAnalyticsTest {
    @Test
    fun `medal tables count exact medals in their displayed level bands`() {
        val high = chart(1, "High", "14+", 14.8)
        val lower = chart(2, "Lower", "10+", 10.8)
        val utage = chart(3, "Utage", "13+?", 13.6, type = "utage")
        val unratedUtage = chart(4, "Unrated", "*", 0.0, type = "utage")
        val beginner = chart(5, "Beginner", "1", 1.0)
        val summary = ScoreAnalytics.summarize(
            listOf(high, lower, utage, unratedUtage, beginner),
            mapOf(
                high.chartKey to score(high, combo = ComboMedal.AP_PLUS, sync = SyncMedal.FDX_PLUS),
                lower.chartKey to score(lower, combo = ComboMedal.FC, sync = SyncMedal.FS),
                utage.chartKey to score(utage, combo = ComboMedal.AP, sync = SyncMedal.FDX),
                unratedUtage.chartKey to score(unratedUtage, combo = ComboMedal.FC_PLUS, sync = SyncMedal.FS_PLUS),
                beginner.chartKey to score(beginner, combo = ComboMedal.FC_PLUS),
            ),
        )

        assertEquals(5, summary.playedCharts)
        assertEquals(ScoreAnalytics.standardLevelBands, summary.levelTables.map(MedalLevelTable::levels))
        assertEquals(1, summary.tableFor("14+").count("AP+", "14+"))
        assertEquals(0, summary.tableFor("14+").count("AP", "14+"))
        assertEquals(1, summary.tableFor("10+").count("FC", "10+"))
        assertEquals(1, summary.tableFor("10+").count("FS", "10+"))
        assertEquals(1, summary.tableFor("1").count("FC+", "1"))
        assertEquals(1, summary.utage.count("AP", "13+?"))
        assertEquals(1, summary.utage.count("FDX", "13+?"))
        assertEquals(1, summary.utage.count("FC+", "*"))
    }

    @Test
    fun `best metrics prefer the hardest exact medal and highest DX percentage`() {
        val easySssPlus = chart(1, "Easy SSS+", "12", 12.4)
        val hardSssPlus = chart(2, "Hard SSS+", "14", 14.5)
        val ap = chart(3, "AP", "14+", 14.8)
        val apPlus = chart(4, "AP+", "15", 15.0)
        val fdx = chart(5, "FDX", "13+", 13.9)
        val fdxPlus = chart(6, "FDX+", "14", 14.2)
        val bestPercentage = chart(7, "Best percentage", "11", 11.0)
        val charts = listOf(easySssPlus, hardSssPlus, ap, apPlus, fdx, fdxPlus, bestPercentage)
        val scores = mapOf(
            easySssPlus.chartKey to score(easySssPlus, grade = Grade.SSS_PLUS),
            hardSssPlus.chartKey to score(hardSssPlus, grade = Grade.SSS_PLUS),
            ap.chartKey to score(ap, combo = ComboMedal.AP),
            apPlus.chartKey to score(apPlus, combo = ComboMedal.AP_PLUS),
            fdx.chartKey to score(fdx, sync = SyncMedal.FDX),
            fdxPlus.chartKey to score(fdxPlus, sync = SyncMedal.FDX_PLUS),
            bestPercentage.chartKey to score(bestPercentage, dxScore = 999, maxDxScore = 1_000),
        )
        val best = ScoreAnalytics.summarize(charts, scores).bestScores.associateBy(BestScore::metric)

        assertEquals("Hard SSS+", best.getValue(BestScoreMetric.SSS_PLUS).chart?.title)
        assertEquals("AP", best.getValue(BestScoreMetric.AP).chart?.title)
        assertEquals("AP+", best.getValue(BestScoreMetric.AP_PLUS).chart?.title)
        assertEquals("FDX", best.getValue(BestScoreMetric.FDX).chart?.title)
        assertEquals("FDX+", best.getValue(BestScoreMetric.FDX_PLUS).chart?.title)
        assertEquals("Best percentage", best.getValue(BestScoreMetric.DX_SCORE).chart?.title)
    }

    private fun ScoreAnalyticsSummary.tableFor(level: String): MedalLevelTable =
        levelTables.single { level in it.levels }

    private fun MedalLevelTable.count(row: String, level: String): Int {
        val rowIndex = rows.indexOfFirst { it.label == row }
        val levelIndex = levels.indexOf(level)
        return rows[rowIndex].counts[levelIndex]
    }

    private fun score(
        chart: SongChart,
        grade: Grade = Grade.S,
        combo: ComboMedal = ComboMedal.NONE,
        sync: SyncMedal = SyncMedal.NONE,
        dxScore: Int = 900,
        maxDxScore: Int = 1_000,
    ) = UserScore(
        chartKey = chart.chartKey,
        achievement = grade.threshold,
        grade = grade,
        comboMedal = combo,
        syncMedal = sync,
        dxScore = dxScore,
        maxDxScore = maxDxScore,
    )

    private fun chart(
        id: Long,
        title: String,
        level: String,
        constant: Double,
        type: String = "dx",
    ) = SongChart(
        id = id,
        chartKey = title,
        sourceSongId = title,
        category = "maimai",
        title = title,
        titleRomanized = title,
        titleAliases = "",
        artist = "Artist",
        artistRomanized = "Artist",
        bpm = 120,
        imageUrl = "",
        songVersion = "Version",
        releaseDate = "2026-01-01",
        comment = null,
        type = type,
        difficulty = if (type == "utage") "【宴】" else "master",
        level = level,
        levelValue = constant,
        constant = constant.takeUnless { type == "utage" },
        noteDesigner = null,
        noteDesignerRomanized = "",
        noteCounts = NoteCounts(null, null, null, null, null, null),
        regions = Regions(jp = true, international = true, usa = false, china = false),
        chartVersion = "Version",
        isSpecial = type == "utage",
    )
}
