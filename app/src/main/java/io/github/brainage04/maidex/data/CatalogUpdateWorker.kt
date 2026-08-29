package io.github.brainage04.maidex.data

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CatalogUpdateWorker(
    appContext: Context,
    parameters: WorkerParameters,
) : CoroutineWorker(appContext, parameters) {
    override suspend fun doWork(): Result = try {
        withContext(Dispatchers.IO) {
            CatalogRepository(applicationContext).refreshFromSource()
        }
        Result.success()
    } catch (_: Exception) {
        if (runAttemptCount < 2) Result.retry() else Result.success()
    }
}

object CatalogUpdateScheduler {
    private const val WORK_NAME = "hourly-catalog-update"

    fun schedule(context: Context) {
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<CatalogUpdateWorker>(
                CATALOG_UPDATE_INTERVAL_HOURS,
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
}

internal const val CATALOG_UPDATE_INTERVAL_HOURS = 1L
