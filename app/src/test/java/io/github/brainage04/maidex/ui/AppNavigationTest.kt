package io.github.brainage04.maidex.ui

import io.github.brainage04.maidex.data.CircleDailySnapshot
import io.github.brainage04.maidex.data.CircleData
import io.github.brainage04.maidex.data.CircleMember
import io.github.brainage04.maidex.data.ThemeMode
import io.github.brainage04.maidex.data.UnlockGuideSection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppNavigationTest {
    @Test
    fun `back stack pushes screens on top and pops them in reverse order`() {
        val stack = AppBackStack()
        stack.push(AppScreen.DanGuide)
        stack.push(AppScreen.ChartDetail("song|dx|master"))

        assertEquals(AppScreen.ChartDetail("song|dx|master"), stack.top)
        assertTrue(stack.pop())
        assertEquals(AppScreen.DanGuide, stack.top)
        assertTrue(stack.pop())
        assertNull(stack.top)
        assertFalse(stack.pop())
    }

    @Test
    fun `opening the screen that is already on top does not stack it twice`() {
        val stack = AppBackStack()
        stack.push(AppScreen.Settings)
        stack.push(AppScreen.Settings)

        assertEquals(1, stack.entries.size)
    }

    @Test
    fun `every screen key restores the same screen`() {
        val screens = listOf(
            AppScreen.Account,
            AppScreen.Circle,
            AppScreen.ScoreStats,
            AppScreen.DanGuide,
            AppScreen.Filters,
            AppScreen.PlayCountHistory,
            AppScreen.ProfileCard,
            AppScreen.About,
            AppScreen.Settings,
            AppScreen.UnlockGuide(UnlockGuideSection.CHIHOS),
            AppScreen.UnlockGuide(UnlockGuideSection.CLASS_BATTLES, "class-prism"),
            AppScreen.ChartDetail("title:with:colons|dx|master"),
        )

        screens.forEach { screen -> assertEquals(screen, AppScreen.fromKey(screen.key)) }
        assertNull(AppScreen.fromKey("unknown"))
    }

    @Test
    fun `theme defaults to the system setting and maps each mode`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.DEFAULT)
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromStored(null))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromStored("PURPLE"))
        assertEquals(ThemeMode.DARK, ThemeMode.fromStored("DARK"))
        assertTrue(ThemeMode.SYSTEM.isDark(systemDark = true))
        assertFalse(ThemeMode.SYSTEM.isDark(systemDark = false))
        assertFalse(ThemeMode.LIGHT.isDark(systemDark = true))
        assertTrue(ThemeMode.DARK.isDark(systemDark = false))
    }

    @Test
    fun `member trends put the signed-in player first, then the others by latest points`() {
        val snapshots = listOf(
            snapshot("2026-11-01", member("me", 10, you = true), member("a", 50), member("b", 5)),
            snapshot("2026-11-02", member("me", 20, you = true), member("a", 60), member("b", 80)),
        )

        val trends = circleMemberTrends(snapshots)

        assertEquals(listOf("me", "b", "a"), trends.map { it.key })
        assertEquals(listOf("2026-11-01" to 10, "2026-11-02" to 20), trends.first().points)
    }

    @Test
    fun `member trends skip days a member was absent and keep their latest name`() {
        val snapshots = listOf(
            snapshot("2026-11-01", member("a", 5, name = "OLD")),
            snapshot("2026-11-02"),
            snapshot("2026-11-03", member("a", 9, name = "NEW")),
        )

        val trend = circleMemberTrends(snapshots).single()

        assertEquals("NEW", trend.name)
        assertEquals(listOf("2026-11-01" to 5, "2026-11-03" to 9), trend.points)
    }

    private fun member(key: String, points: Int, you: Boolean = false, name: String = key.uppercase()) =
        CircleMember(key = key, name = name, points = points, isCurrentUser = you)

    private fun snapshot(day: String, vararg members: CircleMember) = CircleDailySnapshot(
        day = day,
        capturedAt = 0L,
        circle = CircleData(month = day.take(7), name = "Circle", members = members.toList()),
    )
}
