package io.github.brainage04.maidex.autologin

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.webkit.CookieManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import io.github.brainage04.maidex.MainActivity
import io.github.brainage04.maidex.R
import io.github.brainage04.maidex.data.AccountRegion
import io.github.brainage04.maidex.data.CatalogRepository
import io.github.brainage04.maidex.data.UserDataRepository
import io.github.brainage04.maidex.network.MaimaiDxClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

/** Runs [AutoUpdateRunner] in the background so opening the app never waits on DX NET. */
class AutoUpdateWorker(
    appContext: Context,
    parameters: WorkerParameters,
) : CoroutineWorker(appContext, parameters) {
    override suspend fun doWork(): Result {
        val notifier = AutoUpdateNotifier(applicationContext)
        return try {
            val users = UserDataRepository(applicationContext)
            val region = withContext(Dispatchers.IO) { users.loadProfile()?.region }
                ?: AccountRegion.INTERNATIONAL
            val client = MaimaiDxClient(withContext(Dispatchers.Main) { CookieManager.getInstance() })
            val refresh = DxNetRefresh { accountRegion, onProgress ->
                val charts = withContext(Dispatchers.IO) { CatalogRepository(applicationContext).load().charts }
                val result = client.import(accountRegion, charts, onProgress)
                withContext(Dispatchers.IO) { users.saveImport(result) }
                RefreshResult(
                    summary = "Updated ${result.scores.size} scores and ${result.playDetails.size} recent plays",
                    scores = result.scores.size,
                    recentDetails = result.playDetails.size,
                    unmatched = result.unmatchedCharts,
                )
            }
            val reporter = object : AutoUpdateReporter {
                override fun started() {
                    notifier.showRunning(STARTED_MESSAGE)
                    setProgressAsync(workDataOf(KEY_MESSAGE to STARTED_MESSAGE))
                }

                override fun progress(message: String) {
                    notifier.showRunning(message)
                    setProgressAsync(workDataOf(KEY_MESSAGE to message))
                }

                override fun finished(outcome: AutoUpdateOutcome.Finished) = notifier.showFinished(outcome)
            }
            val runner = AutoUpdateRunner(
                AutoLoginStore.create(applicationContext),
                DxNetSignIn { accountRegion, credentials -> client.signInWithSegaId(accountRegion, credentials) },
                reporter,
            )
            val outcome = withContext(Dispatchers.IO) {
                runner.run(region, refresh, force = inputData.getBoolean(KEY_FORCE, false))
            }
            // Never Result.retry(): a failed automatic update waits for the next app open.
            Result.success(
                Data.Builder()
                    .putAll(outcome.toData())
                    .putLong(KEY_FINISHED_AT, System.currentTimeMillis())
                    .build(),
            )
        } catch (cancelled: CancellationException) {
            notifier.dismiss()
            throw cancelled
        }
    }

    companion object {
        const val KEY_FORCE = "force"
        const val KEY_MESSAGE = "message"
        const val KEY_OUTCOME = "outcome"
        const val KEY_SCORES = "scores"
        const val KEY_RECENT = "recent"
        const val KEY_UNMATCHED = "unmatched"
        const val KEY_FINISHED_AT = "finished_at"
        const val OUTCOME_UPDATED = "updated"
        const val OUTCOME_FAILED = "failed"
        const val OUTCOME_SKIPPED = "skipped"
        const val STARTED_MESSAGE = "Updating your DX NET data…"
    }
}

private fun AutoUpdateOutcome.toData(): Data = when (this) {
    is AutoUpdateOutcome.Updated -> workDataOf(
        AutoUpdateWorker.KEY_OUTCOME to AutoUpdateWorker.OUTCOME_UPDATED,
        AutoUpdateWorker.KEY_MESSAGE to summary,
        AutoUpdateWorker.KEY_SCORES to result.scores,
        AutoUpdateWorker.KEY_RECENT to result.recentDetails,
        AutoUpdateWorker.KEY_UNMATCHED to result.unmatched,
    )
    is AutoUpdateOutcome.Failed -> workDataOf(
        AutoUpdateWorker.KEY_OUTCOME to AutoUpdateWorker.OUTCOME_FAILED,
        AutoUpdateWorker.KEY_MESSAGE to message,
    )
    is AutoUpdateOutcome.Skipped -> workDataOf(AutoUpdateWorker.KEY_OUTCOME to AutoUpdateWorker.OUTCOME_SKIPPED)
}

object AutoUpdateScheduler {
    const val WORK_NAME = "dx-net-auto-update"

    /** Queues one automatic update; a run that is already queued or running is kept. */
    fun request(context: Context, force: Boolean = false) {
        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<AutoUpdateWorker>()
                .setInputData(workDataOf(AutoUpdateWorker.KEY_FORCE to force))
                .build(),
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}

/** Android notification marking the start and end (success or failure) of an automatic update. */
class AutoUpdateNotifier(private val context: Context) {
    private val manager = NotificationManagerCompat.from(context)

    fun showRunning(message: String) = post(
        base()
            .setContentTitle("Updating maimai DX NET data")
            .setContentText(message)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(0, 0, true),
    )

    fun showFinished(outcome: AutoUpdateOutcome.Finished) = post(
        base()
            .setContentTitle(
                if (outcome is AutoUpdateOutcome.Updated) "maimai DX NET data updated" else "maimai DX NET update failed",
            )
            .setContentText(outcome.summary)
            .setStyle(NotificationCompat.BigTextStyle().bigText(outcome.summary))
            .setAutoCancel(true),
    )

    fun dismiss() = manager.cancel(NOTIFICATION_ID)

    private fun base(): NotificationCompat.Builder {
        ensureChannel()
        val open = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentIntent(open)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
    }

    @SuppressLint("MissingPermission") // Checked by canPost().
    private fun post(builder: NotificationCompat.Builder) {
        if (canPost()) manager.notify(NOTIFICATION_ID, builder.build())
    }

    private fun canPost(): Boolean =
        manager.areNotificationsEnabled() &&
            (
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
                )

    private fun ensureChannel() {
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "DX NET automatic update", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Shows when MaiDex signs in and refreshes your DX NET data in the background"
            },
        )
    }

    private companion object {
        const val CHANNEL_ID = "dx_net_auto_update"
        const val NOTIFICATION_ID = 4201
    }
}
