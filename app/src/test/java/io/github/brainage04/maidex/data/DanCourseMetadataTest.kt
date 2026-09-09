package io.github.brainage04.maidex.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DanCourseMetadataTest {
    @Test
    fun `current version contains every fixed four-track course`() {
        assertEquals("CiRCLE PLUS", DanCourseMetadata.version)
        assertEquals(22, DanCourseMetadata.courses.size)
        assertEquals(10, DanCourseMetadata.courses.count { it.group == DanCourseGroup.NORMAL })
        assertEquals(11, DanCourseMetadata.courses.count { it.group == DanCourseGroup.TRUE })
        assertEquals(1, DanCourseMetadata.courses.count { it.group == DanCourseGroup.URA })
        assertTrue(DanCourseMetadata.courses.all { it.tracks.size == 4 })
    }

    @Test
    fun `international course differences are explicit and limited`() {
        val changed = DanCourseMetadata.courses.flatMap { course ->
            course.tracks.indices.mapNotNull { index ->
                val japan = course.track(index, AccountRegion.JAPAN)
                val international = course.track(index, AccountRegion.INTERNATIONAL)
                if (japan != international) course.id to index else null
            }
        }

        assertEquals(listOf("first" to 0, "eighth" to 1, "true-fourth" to 1), changed)
    }

    @Test
    fun `true and ura courses use their strict life rules`() {
        val trueCourses = DanCourseMetadata.courses.filter { it.group == DanCourseGroup.TRUE }
        assertTrue(trueCourses.all { it.life.maximum == 50 })
        assertTrue(trueCourses.all { it.life.greatDamage == 2 })
        assertTrue(trueCourses.all { it.life.goodDamage == 3 })
        assertTrue(trueCourses.all { it.life.missDamage == 5 })
        assertEquals(5, trueCourses.single { it.id == "true-kaiden" }.life.trackBonus)

        val ura = DanCourseMetadata.courses.single { it.group == DanCourseGroup.URA }
        assertEquals(DanLifeRule(10, 1, 3, 10, 0), ura.life)
        assertNotEquals(
            DanCourseMetadata.chartKey(ura.track(0, AccountRegion.JAPAN)),
            DanCourseMetadata.chartKey(ura.track(1, AccountRegion.JAPAN)),
        )
    }

    @Test
    fun `historical courses retain their own charts and life bonuses`() {
        val splashTenth = DanCourseMetadata.coursesForVersion("Splash PLUS").single { it.id == "tenth" }
        assertEquals(
            listOf(
                DanTrack("天火明命", "std", "master"),
                DanTrack("Ether Strike", "dx", "master"),
                DanTrack("Lividi", "std", "master"),
                DanTrack("VERTeX", "std", "master"),
            ),
            splashTenth.tracks,
        )
        assertEquals(20, splashTenth.life.trackBonus)
        assertEquals(
            50,
            DanCourseMetadata.coursesForVersion("FESTiVAL").single { it.id == "tenth" }.life.trackBonus,
        )
        assertEquals(30, DanCourseMetadata.courses.single { it.id == "tenth" }.life.trackBonus)
        assertTrue(DanCourseMetadata.coursesForVersion("UNiVERSE PLUS").none { it.group == DanCourseGroup.URA })
        assertEquals(
            "ura-kaiden",
            DanCourseMetadata.coursesForVersion("FESTiVAL").single { it.group == DanCourseGroup.URA }.id,
        )
    }

    @Test
    fun `historical international substitutions do not alter Japanese courses`() {
        val splashThird = DanCourseMetadata.coursesForVersion("Splash PLUS").single { it.id == "third" }
        assertEquals(DanTrack("アゲアゲアゲイン", "std", "advanced"), splashThird.track(1, AccountRegion.JAPAN))
        assertEquals(
            DanTrack("囲い無き世は一期の月影", "std", "advanced"),
            splashThird.track(1, AccountRegion.INTERNATIONAL),
        )
        assertEquals(
            DanTrack("テレキャスタービーボーイ", "dx", "advanced"),
            DanCourseMetadata.coursesForVersion("UNiVERSE PLUS")
                .single { it.id == "third" }.track(1, AccountRegion.INTERNATIONAL),
        )
    }

    @Test(expected = NoSuchElementException::class)
    fun `unknown releases cannot silently show current courses`() {
        DanCourseMetadata.coursesForVersion("Splash")
    }
}
