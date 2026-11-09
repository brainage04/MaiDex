package io.github.brainage04.maidex.ui

import io.github.brainage04.maidex.data.AccountRegion
import io.github.brainage04.maidex.data.ComboMedal
import io.github.brainage04.maidex.data.DanCourseGroup
import io.github.brainage04.maidex.data.DanCourseMetadata
import io.github.brainage04.maidex.data.DanTrack
import io.github.brainage04.maidex.data.Grade
import io.github.brainage04.maidex.data.NoteCounts
import io.github.brainage04.maidex.data.Regions
import io.github.brainage04.maidex.data.SongChart
import io.github.brainage04.maidex.data.SyncMedal
import io.github.brainage04.maidex.data.UnlockGuideSection
import io.github.brainage04.maidex.data.UnlockMetadata
import io.github.brainage04.maidex.data.UserScore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GuideSearchTest {
    private val chihos = UnlockMetadata.guideEntries.filter { it.section == UnlockGuideSection.CHIHOS }
    private val classBattles = UnlockMetadata.guideEntries.filter { it.section == UnlockGuideSection.CLASS_BATTLES }

    @Test
    fun `blank chiho query keeps every entry in order`() {
        assertEquals(chihos, filterUnlockGuideEntries(chihos, "  ", emptyMap()))
    }

    @Test
    fun `chiho search matches area titles, romanised titles and unlock songs`() {
        assertEquals(
            listOf("chiho-ongeki-9"),
            filterUnlockGuideEntries(chihos, "ongeki area", emptyMap()).map { it.id },
        )
        assertEquals(
            listOf("chiho-ongeki-9"),
            filterUnlockGuideEntries(chihos, "daredevil", emptyMap()).map { it.id },
        )
        assertEquals(
            listOf("chiho-tricoro-2"),
            filterUnlockGuideEntries(chihos, "ソテリア", emptyMap()).map { it.id },
        )
    }

    @Test
    fun `chiho search uses the catalog romanisation of unlock songs`() {
        val romanized = mapOf("ソテリア" to "Soteria")

        assertEquals(
            listOf("chiho-tricoro-2"),
            filterUnlockGuideEntries(chihos, "soteria", romanized).map { it.id },
        )
    }

    @Test
    fun `class battle search matches the version, the Gift Song and boss songs`() {
        assertTrue(
            filterUnlockGuideEntries(classBattles, "Cryptarithm", emptyMap()).any { it.id == "class-prism" },
        )
        assertEquals(
            listOf("class-buddies-plus"),
            filterUnlockGuideEntries(classBattles, "buddies plus", emptyMap()).map { it.id },
        )
        val prismBoss = io.github.brainage04.maidex.data.ClassBattleMetadata.byVersion.getValue("PRiSM").last()
        assertTrue(
            filterUnlockGuideEntries(classBattles, prismBoss.songTitle, emptyMap()).any { it.id == "class-prism" },
        )
    }

    @Test
    fun `unmatched query returns nothing`() {
        assertTrue(filterUnlockGuideEntries(chihos, "zzzz no such song", emptyMap()).isEmpty())
    }

    @Test
    fun `dan search finds courses by track title across every Dan type of one version`() {
        val courses = DanCourseMetadata.coursesForVersion("CiRCLE PLUS")
        val normal = courses.first { it.group == DanCourseGroup.NORMAL }
        val trueCourse = courses.first { it.group == DanCourseGroup.TRUE }

        assertTrue(filterDanCourses(courses, normal.tracks.first().title, AccountRegion.JAPAN, emptyMap()).contains(normal))
        assertTrue(
            filterDanCourses(courses, trueCourse.tracks.last().title, AccountRegion.JAPAN, emptyMap()).contains(trueCourse),
        )
    }

    @Test
    fun `dan search matches course names in English and Japanese`() {
        val courses = DanCourseMetadata.coursesForVersion("CiRCLE PLUS")

        val english = filterDanCourses(courses, "first dan", AccountRegion.JAPAN, emptyMap())
        val japanese = filterDanCourses(courses, "初段", AccountRegion.JAPAN, emptyMap())

        assertTrue(english.any { it.id == "first" })
        assertTrue(english.all { it.nameEnglish.lowercase().contains("first dan") })
        assertTrue(japanese.any { it.id == "first" })
        assertTrue(japanese.all { it.name.contains("初段") })
        assertTrue(english.size < courses.size)
    }

    @Test
    fun `dan search uses the romanised title of a track's chart`() {
        val courses = DanCourseMetadata.coursesForVersion("CiRCLE PLUS")
        val track = courses.first().tracks.first()
        val lookup = mapOf(DanCourseMetadata.chartKey(track) to chart(track.title, romanized = "Hello Planet Romanised"))

        assertTrue(
            filterDanCourses(courses, "hello planet romanised", AccountRegion.JAPAN, lookup).contains(courses.first()),
        )
    }

    @Test
    fun `guide song chart prefers the best scored chart, then DX master`() {
        val expert = chart("Song", difficulty = "expert", key = "song|dx|expert", level = "12")
        val stdMaster = chart("Song", type = "std", key = "song|std|master", level = "13")
        val dxMaster = chart("Song", key = "song|dx|master", level = "13+")
        val charts = listOf(expert, stdMaster, dxMaster)

        assertEquals(dxMaster, guideSongChart("Song", charts, emptyMap()))
        assertEquals(expert, guideSongChart("Song", charts, mapOf(expert.chartKey to score(expert.chartKey))))
        assertEquals(stdMaster, guideSongChart("Song", charts, emptyMap(), level = "13"))
        assertNull(guideSongChart("Other", charts, emptyMap()))
    }

    @Test
    fun `dan track label leaves out an unknown level instead of a placeholder`() {
        val track = DanTrack("Song", "dx", "master")

        assertEquals("DX · MASTER", danTrackLabel(track, null))
        assertEquals("DX · MASTER 13+ (13.7)", danTrackLabel(track, chart("Song", level = "13+", constant = 13.7)))
    }

    @Test
    fun `chiho status explains progress without the unconfirmed-map wording`() {
        assertEquals("Map complete on DX NET", chihoStatusText(1, 4, mapComplete = true))
        assertEquals("2/4 unlock songs played", chihoStatusText(2, 4, mapComplete = false))
    }

    private fun score(key: String) = UserScore(
        chartKey = key,
        achievement = 100.0,
        grade = Grade.SSS,
        comboMedal = ComboMedal.NONE,
        syncMedal = SyncMedal.NONE,
        dxScore = 0,
        maxDxScore = 0,
    )

    private fun chart(
        title: String,
        romanized: String = title,
        type: String = "dx",
        difficulty: String = "master",
        key: String = "$title|$type|$difficulty",
        level: String? = "13",
        constant: Double? = null,
    ) = SongChart(
        id = key.hashCode().toLong(),
        chartKey = key,
        sourceSongId = title,
        category = "maimai",
        title = title,
        titleRomanized = romanized,
        titleAliases = "",
        artist = "Artist",
        artistRomanized = "Artist",
        bpm = 120,
        imageUrl = "",
        songVersion = "Version",
        releaseDate = "2026-01-01",
        comment = null,
        type = type,
        difficulty = difficulty,
        level = level,
        levelValue = level?.removeSuffix("+")?.toDoubleOrNull(),
        constant = constant,
        noteDesigner = null,
        noteDesignerRomanized = "",
        noteCounts = NoteCounts(null, null, null, null, null, null),
        regions = Regions(jp = true, international = true, usa = false, china = false),
        chartVersion = "Version",
        isSpecial = false,
    )
}
