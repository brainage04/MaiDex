package io.github.brainage04.maidex.data

import org.junit.Assert.assertEquals
import org.junit.Test

class SearchNormalizationTest {
    @Test
    fun `normalization keeps compatibility without regex allocation`() {
        assertEquals("abcdefg", normalizeSearch(" ＡＢＣ-\u00a0d_e/f.g! "))
        assertEquals("ガ", normalizeSearch("カ\u3099"))
    }
}
