package io.github.brainage04.maidex.ui

import io.github.brainage04.maidex.network.DX_NET_MAINTENANCE_MESSAGE
import org.junit.Assert.assertEquals
import org.junit.Test

class CircleSyncStatusTest {
    @Test
    fun `maintenance is shown as maintenance, not as a failed sync`() {
        assertEquals(
            "DX NET is under maintenance; try again later",
            circleSyncStatus(isSyncing = false, lastError = DX_NET_MAINTENANCE_MESSAGE, lastSuccessAt = 0L),
        )
    }

    @Test
    fun `an expired login is still a failed sync`() {
        assertEquals(
            "Last sync failed: DX NET login expired; sign in again",
            circleSyncStatus(isSyncing = false, lastError = "DX NET login expired; sign in again", lastSuccessAt = 0L),
        )
    }

    @Test
    fun `syncing wins over the last error`() {
        assertEquals("Syncing now…", circleSyncStatus(isSyncing = true, lastError = DX_NET_MAINTENANCE_MESSAGE, lastSuccessAt = 0L))
    }
}
