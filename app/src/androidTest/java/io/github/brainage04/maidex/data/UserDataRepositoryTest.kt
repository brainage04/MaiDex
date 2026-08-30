package io.github.brainage04.maidex.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserDataRepositoryTest {
    @Test
    fun trackingSnapshotsKeepDailyPointsPlayCountsAndMonthlyHistory() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = UserDataRepository(context)
        repository.clear()
        try {
            saveSnapshot(
                repository = repository,
                month = "2026-07",
                capturedAt = Instant.parse("2026-07-31T07:00:00Z").toEpochMilli(),
                circlePoints = 10_000,
                memberPoints = 6_000,
                currentPlayCount = 42,
                totalPlayCount = 2_190,
            )
            saveSnapshot(
                repository = repository,
                month = "2026-08",
                capturedAt = Instant.parse("2026-08-01T07:00:00Z").toEpochMilli(),
                circlePoints = 800,
                memberPoints = 500,
                currentPlayCount = 3,
                totalPlayCount = 2_193,
            )

            assertEquals(listOf("2026-08", "2026-07"), repository.loadCircleHistory().map { it.month })
            assertEquals(listOf(10_000, 800), repository.loadCircleSnapshots().map { it.circle.totalPoints })
            assertEquals(listOf(6_000, 500), repository.loadCircleSnapshots().map { it.circle.members.single().points })
            assertEquals(listOf(42, 3), repository.loadPlayCountSnapshots().map { it.currentVersionPlayCount })
            assertEquals(listOf(2_190, 2_193), repository.loadPlayCountSnapshots().map { it.totalPlayCount })
            assertEquals(3, repository.loadProfile()?.currentVersionPlayCount)
            assertEquals(2_193, repository.loadProfile()?.totalPlayCount)
        } finally {
            repository.clear()
        }
    }

    @Test
    fun trackingSnapshotsPreserveCompletedChihosAndLatestFriendClass() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = UserDataRepository(context)
        repository.clear()
        try {
            repository.saveTrackingSnapshot(
                profile = progressProfile(
                    completedChihos = setOf("トリコロちほー"),
                    friendClass = "S5",
                ),
                circle = null,
            )
            repository.saveTrackingSnapshot(
                profile = progressProfile(),
                circle = null,
            )

            assertEquals(setOf("トリコロちほー"), repository.loadProfile()?.completedChihoNames)
            assertEquals("S5", repository.loadProfile()?.friendClass)

            repository.saveTrackingSnapshot(
                profile = progressProfile(
                    completedChihos = setOf("オンゲキちほー9"),
                    friendClass = "SS1",
                ),
                circle = null,
            )

            assertEquals(
                setOf("トリコロちほー", "オンゲキちほー9"),
                repository.loadProfile()?.completedChihoNames,
            )
            assertEquals("SS1", repository.loadProfile()?.friendClass)
        } finally {
            repository.clear()
        }
    }

    @Test
    fun filterPresetsPersistIndependentlyFromImportedAccountData() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = UserDataRepository(context)
        val preset = FilterPreset(
            id = "instrumentation-preset",
            name = "My level 14",
            filters = ChartFilters(
                search = "must not persist",
                minLevel = 14.0,
                categories = setOf("POPS & ANIME"),
                comboMedals = setOf(ComboMedal.AP, ComboMedal.AP_PLUS),
                scoredOnly = true,
            ),
            sort = ChartSort.ACHIEVEMENT,
            sortOrder = SortOrder.DESCENDING,
        )
        repository.deleteFilterPreset(preset.id)
        try {
            repository.saveFilterPreset(preset.copy(filters = preset.filters.copy(search = "")))

            val reloaded = UserDataRepository(context).loadFilterPresets().single {
                it.id == preset.id
            }
            assertEquals(preset.copy(filters = preset.filters.copy(search = "")), reloaded)

            repository.clear()
            assertEquals(
                preset.id,
                UserDataRepository(context).loadFilterPresets().single { it.id == preset.id }.id,
            )
        } finally {
            repository.deleteFilterPreset(preset.id)
        }
    }

    private fun progressProfile(
        completedChihos: Set<String> = emptySet(),
        friendClass: String = "",
    ) = PlayerProfile(
        name = "Player",
        officialRating = 16_000,
        region = AccountRegion.INTERNATIONAL,
        completedChihoNames = completedChihos,
        friendClass = friendClass,
        importedAt = Instant.parse("2026-08-03T07:00:00Z").toEpochMilli(),
    )

    private fun saveSnapshot(
        repository: UserDataRepository,
        month: String,
        capturedAt: Long,
        circlePoints: Int,
        memberPoints: Int,
        currentPlayCount: Int,
        totalPlayCount: Int,
    ) {
        repository.saveTrackingSnapshot(
            profile = PlayerProfile(
                name = "Player",
                officialRating = 16_000,
                region = AccountRegion.INTERNATIONAL,
                currentVersionPlayCount = currentPlayCount,
                totalPlayCount = totalPlayCount,
                importedAt = capturedAt,
            ),
            circle = CircleData(
                month = month,
                name = "Test Circle",
                code = "ABCDEFGH",
                totalPoints = circlePoints,
                members = listOf(
                    CircleMember(
                        key = "player",
                        name = "Player",
                        points = memberPoints,
                        isCurrentUser = true,
                    ),
                ),
                importedAt = capturedAt,
            ),
        )
    }
}
