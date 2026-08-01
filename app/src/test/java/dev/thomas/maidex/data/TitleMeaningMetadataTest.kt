package dev.thomas.maidex.data

import org.junit.Assert.assertEquals
import org.junit.Test

class TitleMeaningMetadataTest {
    @Test
    fun `verified English meaning is separate from the Japanese reading`() {
        assertEquals(
            "The Gravekeeper of a Dead Tree",
            TitleMeaningMetadata.forSong("躯樹の墓守"),
        )
        assertEquals("", TitleMeaningMetadata.forSong("系ぎて"))
    }
}
