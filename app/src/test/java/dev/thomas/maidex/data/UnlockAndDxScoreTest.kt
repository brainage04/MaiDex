package dev.thomas.maidex.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UnlockAndDxScoreTest {
    @Test
    fun `Daredevil Glaive uses the distance unlock rather than Perfect Challenge`() {
        val info = UnlockMetadata.forSong("Daredevil Glaive").single()

        assertEquals("Chiho unlock", info.label)
        assertTrue(info.summary.contains("525 km"))
        assertTrue(info.details.contains("not the area's Perfect Challenge track"))
        assertEquals("chiho-ongeki-9", info.guideEntryId)
    }

    @Test
    fun `7 Wonders preserves its complete easing history`() {
        val info = UnlockMetadata.forSong("7 Wonders").single()

        assertTrue(info.details.contains("SSS1"))
        assertTrue(info.details.contains("SSS5+"))
        assertTrue(info.details.contains("SS5+"))
        assertTrue(info.details.contains("available by default from CiRCLE PLUS"))
        assertEquals("class-circle", info.guideEntryId)
    }

    @Test
    fun `songs without curated unlock requirements remain unmarked`() {
        assertTrue(UnlockMetadata.forSong("PANDORA PARADOXXX").isEmpty())
        assertFalse(UnlockMetadata.forSong("Sky Trails").isEmpty())
    }

    @Test
    fun `unlock guide exposes separate Chiho and class battle lists`() {
        val chihos = UnlockMetadata.guideEntries.filter { it.section == UnlockGuideSection.CHIHOS }
        val classBattles = UnlockMetadata.guideEntries.filter { it.section == UnlockGuideSection.CLASS_BATTLES }

        assertEquals(9, chihos.size)
        assertTrue(chihos.any { entry -> entry.songs.any { it.title == "Daredevil Glaive" } })
        assertEquals(12, classBattles.size)
        assertTrue(classBattles.any { entry -> entry.songs.any { it.title == "7 Wonders" } })
    }

    @Test
    fun `every curated unlock card links to a real guide entry`() {
        UnlockMetadata.guideEntries
            .flatMap { it.songs }
            .forEach { song ->
                val info = UnlockMetadata.forSong(song.title).single()
                assertEquals(info.guideEntryId, UnlockMetadata.guideEntry(info.guideEntryId.orEmpty())?.id)
            }
    }

    @Test
    fun `DX score stars use official percentage boundaries`() {
        assertEquals(0, score(8_499).dxStarCount)
        assertEquals(1, score(8_500).dxStarCount)
        assertEquals(2, score(9_000).dxStarCount)
        assertEquals(3, score(9_300).dxStarCount)
        assertEquals(4, score(9_500).dxStarCount)
        assertEquals(5, score(9_700).dxStarCount)
    }

    @Test
    fun `DX score percentage retains precision for display rounding`() {
        val score = score(dxScore = 2_681, maxDxScore = 2_820)

        assertEquals(95.0709219858, score.dxScorePercentage ?: 0.0, 0.0000000001)
        assertEquals(4, score.dxStarCount)
    }

    private fun score(dxScore: Int, maxDxScore: Int = 10_000) = UserScore(
        chartKey = "chart",
        achievement = 100.0,
        grade = Grade.SSS,
        comboMedal = ComboMedal.NONE,
        syncMedal = SyncMedal.NONE,
        dxScore = dxScore,
        maxDxScore = maxDxScore,
    )
}
