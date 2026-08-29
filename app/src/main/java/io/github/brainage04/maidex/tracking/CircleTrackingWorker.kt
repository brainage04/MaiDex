package io.github.brainage04.maidex.tracking

import android.content.Context
import android.webkit.CookieManager
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import io.github.brainage04.maidex.data.UserDataRepository
import io.github.brainage04.maidex.network.AuthenticationRequiredException
import io.github.brainage04.maidex.network.MaimaiDxClient
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
        repository.recordTrackingAttempt()
        return try {
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
            if (shouldRetryHourlySync(failure, runAttemptCount)) Result.retry() else Result.success()
        }
    }
}

object CircleTrackingScheduler {
    private const val WORK_NAME = "hourly-dx-net-tracking"
    private const val LEGACY_WORK_NAME = "daily-dx-net-tracking"

    fun schedule(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(LEGACY_WORK_NAME)
        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<CircleTrackingWorker>(
                TRACKING_REPEAT_INTERVAL_HOURS,
                TimeUnit.HOURS,
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build(),
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
                .build(),
        )
    }

    fun cancel(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(WORK_NAME)
        workManager.cancelUniqueWork(LEGACY_WORK_NAME)
    }
}

internal const val TRACKING_REPEAT_INTERVAL_HOURS = 1L

internal fun shouldRetryHourlySync(failure: Exception, runAttemptCount: Int): Boolean =
    failure !is AuthenticationRequiredException && runAttemptCount < 2
