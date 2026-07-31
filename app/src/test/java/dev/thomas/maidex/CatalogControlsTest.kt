package dev.thomas.maidex

import dev.thomas.maidex.data.ChartFilters
import dev.thomas.maidex.data.ChartSort
import dev.thomas.maidex.data.ComboMedal
import dev.thomas.maidex.data.ConstantAvailability
import dev.thomas.maidex.data.Grade
import dev.thomas.maidex.data.NoteCounts
import dev.thomas.maidex.data.Regions
import dev.thomas.maidex.data.SongChart
import dev.thomas.maidex.data.SortOrder
import dev.thomas.maidex.data.SyncMedal
import dev.thomas.maidex.data.UserScore
import dev.thomas.maidex.ui.titleWithRomanization
import org.junit.Assert.assertEquals
import org.junit.Test

class CatalogControlsTest {
    private val knownLow = chart(1, "Known low", constant = 1.0)
    private val knownHigh = chart(2, "Known high", constant = 14.9)
    private val unknown = chart(3, "Unknown", constant = null, levelValue = 12.0)
    private val utage = chart(4, "UTAGE", constant = 0.0, type = "utage")
    private val charts = listOf(knownLow, knownHigh, unknown, utage)

    @Test
    fun `level sort supports both orders and uses effective levels`() {
        val withoutUtage = ChartFilters(showUtage = false)

        assertEquals(
            listOf("Known low", "Unknown", "Known high"),
            filterAndSort(charts, withoutUtage, ChartSort.LEVEL, SortOrder.ASCENDING, emptyMap())
                .map(SongChart::title),
        )
        assertEquals(
            listOf("Known high", "Unknown", "Known low"),
            filterAndSort(charts, withoutUtage, ChartSort.LEVEL, SortOrder.DESCENDING, emptyMap())
                .map(SongChart::title),
        )
    }

    @Test
    fun `DX score sort supports both orders and leaves unplayed charts last`() {
        val scores = mapOf(
            knownLow.chartKey to score(knownLow, 2_000),
            knownHigh.chartKey to score(knownHigh, 1_000),
        )
        val withoutUtage = ChartFilters(showUtage = false)

        assertEquals(
            listOf("Known high", "Known low", "Unknown"),
            filterAndSort(charts, withoutUtage, ChartSort.DX_SCORE, SortOrder.ASCENDING, scores)
                .map(SongChart::title),
        )
        assertEquals(
            listOf("Known low", "Known high", "Unknown"),
            filterAndSort(charts, withoutUtage, ChartSort.DX_SCORE, SortOrder.DESCENDING, scores)
                .map(SongChart::title),
        )
    }

    @Test
    fun `UTAGE visibility toggle excludes every UTAGE chart`() {
        assertEquals(
            listOf("Known high", "Known low", "Unknown"),
            filterAndSort(
                charts,
                ChartFilters(showUtage = false),
                ChartSort.TITLE,
                SortOrder.ASCENDING,
                emptyMap(),
            ).map(SongChart::title),
        )
    }

    @Test
    fun `constant availability selects both known and unknown charts`() {
        fun titles(availability: ConstantAvailability) = filterAndSort(
            charts,
            ChartFilters(constantAvailability = availability),
            ChartSort.TITLE,
            SortOrder.ASCENDING,
            emptyMap(),
        ).map(SongChart::title)

        assertEquals(4, titles(ConstantAvailability.BOTH).size)
        assertEquals(listOf("Known high", "Known low", "UTAGE"), titles(ConstantAvailability.KNOWN))
        assertEquals(listOf("Unknown"), titles(ConstantAvailability.UNKNOWN))
    }

    @Test
    fun `title formatting shows SilentBlue romanisation without duplicating Latin titles`() {
        assertEquals(
            "零號車輛 (Linghao cheliang)",
            titleWithRomanization("零號車輛", "Linghao cheliang"),
        )
        assertEquals(
            "Daredevil Glaive",
            titleWithRomanization("Daredevil Glaive", "Daredevil Glaive"),
        )
        assertEquals("(no title)", titleWithRomanization("\u3000", "(no title)"))
    }

    private fun score(chart: SongChart, dxScore: Int) = UserScore(
        chartKey = chart.chartKey,
        achievement = 100.0,
        grade = Grade.SSS,
        comboMedal = ComboMedal.NONE,
        syncMedal = SyncMedal.NONE,
        dxScore = dxScore,
        maxDxScore = 3_000,
    )

    private fun chart(
        id: Long,
        title: String,
        constant: Double?,
        levelValue: Double? = constant,
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
        difficulty = "master",
        level = "12",
        levelValue = levelValue,
        constant = constant,
        noteDesigner = null,
        noteDesignerRomanized = "",
        noteCounts = NoteCounts(null, null, null, null, null, null),
        regions = Regions(jp = true, international = true, usa = false, china = false),
        chartVersion = "Version",
        isSpecial = type == "utage",
    )
}
