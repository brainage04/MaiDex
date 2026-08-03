package dev.thomas.maidex.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MedalTableExpansionStateTest {
    @Test
    fun `collapsed table remains collapsed when another table is accessed`() {
        val state = MedalTableExpansionState()

        state["Levels 13–15 · 1/4"] = false
        assertTrue(state["UTAGE estimated levels"])

        assertFalse(state["Levels 13–15 · 1/4"])
    }
}
