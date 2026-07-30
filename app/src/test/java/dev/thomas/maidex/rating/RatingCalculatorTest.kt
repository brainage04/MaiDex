package dev.thomas.maidex.rating

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
}
