package dev.thomas.maidex.tracking

import dev.thomas.maidex.network.AuthenticationRequiredException

import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class CircleTrackingWorkerTest {
    @Test
    fun `daily tracking waits until selected hour`() {
        val now = ZonedDateTime.of(2026, 8, 2, 6, 30, 0, 0, ZoneId.of("Europe/London"))

        assertEquals(
            Duration.ofMinutes(30).toMillis(),
            nextTrackingDelayMillis(now, 7),
        )
    }

    @Test
    fun `daily tracking schedules tomorrow once selected hour has passed`() {
        val now = ZonedDateTime.of(2026, 8, 2, 7, 0, 0, 0, ZoneId.of("Europe/London"))

        assertEquals(
            Duration.ofDays(1).toMillis(),
            nextTrackingDelayMillis(now, 7),
        )
    }

    @Test
    fun `daily sync retries transient failures without waiting until tomorrow`() {
        val failure = IllegalStateException("DX NET temporarily unavailable")

        assertEquals(true, shouldRetryDailySync(failure, runAttemptCount = 0))
        assertEquals(true, shouldRetryDailySync(failure, runAttemptCount = 1))
        assertEquals(false, shouldRetryDailySync(failure, runAttemptCount = 2))
    }

    @Test
    fun `daily sync does not retry an expired login`() {
        assertEquals(
            false,
            shouldRetryDailySync(
                AuthenticationRequiredException("Sign in again"),
                runAttemptCount = 0,
            ),
        )
    }
}
