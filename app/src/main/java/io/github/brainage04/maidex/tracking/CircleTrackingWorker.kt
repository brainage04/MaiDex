package io.github.brainage04.maidex.tracking

import android.content.Context
import android.webkit.CookieManager
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import io.github.brainage04.maidex.data.UserDataRepository
import io.github.brainage04.maidex.network.MaimaiDxClient
import io.github.brainage04.maidex.network.AuthenticationRequiredException
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CircleTrackingWorker(
    appContext: Context,
    parameters: WorkerParameters,
) : CoroutineWorker(appContext, parameters) {
    override suspend fun doWork(): Result {
        val repository = UserDataRepository(applicationContext)
        val profile = withContext(Dispatchers.IO) { repository.loadProfile() }
            ?: return Result.success()
        val settings = repository.loadTrackingSettings()
        repository.recordTrackingAttempt()
        var retrying = false
        val outcome = try {
            val cookieManager = withContext(Dispatchers.Main) { CookieManager.getInstance() }
            val snapshot = MaimaiDxClient(cookieManager).syncAccount(profile.region)
            withContext(Dispatchers.IO) {
                repository.saveTrackingSnapshot(snapshot.profile, snapshot.circle)
                repository.recordTrackingSuccess()
            }
            Result.success()
        } catch (failure: Exception) {
            repository.recordTrackingFailure(
                failure.message ?: "Unable to refresh DX NET tracking data",
            )
            retrying = shouldRetryDailySync(failure, runAttemptCount)
            if (retrying) Result.retry() else Result.success()
        }
        if (!retrying) {
            CircleTrackingScheduler.scheduleNext(applicationContext, settings.syncHour)
        }
        return outcome
    }
}

object CircleTrackingScheduler {
    private const val WORK_NAME = "daily-dx-net-tracking"

    fun schedule(context: Context, hour: Int) {
        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request(hour),
        )
    }

    internal fun scheduleNext(context: Context, hour: Int) {
        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            request(hour),
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    private fun request(hour: Int) = OneTimeWorkRequestBuilder<CircleTrackingWorker>()
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build(),
        )
        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
        .setInitialDelay(
            nextTrackingDelayMillis(ZonedDateTime.now(), hour.coerceIn(0, 23)),
            TimeUnit.MILLISECONDS,
        )
        .build()
}

internal fun shouldRetryDailySync(failure: Exception, runAttemptCount: Int): Boolean =
    failure !is AuthenticationRequiredException && runAttemptCount < 2

internal fun nextTrackingDelayMillis(now: ZonedDateTime, hour: Int): Long {
    var next = now
        .withHour(hour.coerceIn(0, 23))
        .withMinute(0)
        .withSecond(0)
        .withNano(0)
    if (!next.isAfter(now)) next = next.plusDays(1)
    return Duration.between(now, next).toMillis()
}
