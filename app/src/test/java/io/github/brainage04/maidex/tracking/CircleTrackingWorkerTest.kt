package io.github.brainage04.maidex.tracking

import io.github.brainage04.maidex.network.AuthenticationRequiredException

import org.junit.Assert.assertEquals
import org.junit.Test

class CircleTrackingWorkerTest {
    @Test
    fun `tracking repeats once per hour`() {
        assertEquals(1L, TRACKING_REPEAT_INTERVAL_HOURS)
    }

    @Test
    fun `hourly sync retries transient failures`() {
        val failure = IllegalStateException("DX NET temporarily unavailable")

        assertEquals(true, shouldRetryHourlySync(failure, runAttemptCount = 0))
        assertEquals(true, shouldRetryHourlySync(failure, runAttemptCount = 1))
        assertEquals(false, shouldRetryHourlySync(failure, runAttemptCount = 2))
    }

    @Test
    fun `hourly sync does not retry an expired login`() {
        assertEquals(
            false,
            shouldRetryHourlySync(
                AuthenticationRequiredException("Sign in again"),
                runAttemptCount = 0,
            ),
        )
    }
}
