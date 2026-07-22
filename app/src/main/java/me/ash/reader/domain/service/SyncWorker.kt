package me.ash.reader.domain.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import me.ash.reader.domain.model.account.Account
import me.ash.reader.infrastructure.rss.ReaderCacheHelper
import timber.log.Timber

@HiltWorker
class SyncWorker
@AssistedInject
constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val rssService: RssService,
    private val accountService: AccountService,
    private val readerCacheHelper: ReaderCacheHelper,
    private val workManager: WorkManager,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            doWorkInternal()
        } catch (ce: CancellationException) {
            // 留给 WorkManager 处理协程取消，不能吃掉
            throw ce
        } catch (e: Exception) {
            Timber.e(e, "SyncWorker.doWork crashed")
            if (runAttemptCount >= MAX_RETRY_ATTEMPTS) Result.failure()
            else Result.retry()
        }
    }

    private suspend fun doWorkInternal(): Result {
        val data = inputData
        val accountId = data.getInt("accountId", -1)
        if (accountId == -1) return Result.failure()
        val feedId = data.getString("feedId")
        val groupId = data.getString("groupId")

        // 按任务携带的 accountId 分发到对应类型的服务，
        // 避免周期任务在切换账户后仍按“当前账户”类型分发而永久失败
        val account = accountService.getAccountById(accountId) ?: return Result.failure()
        val service = rssService.get(account.type.id)

        return service
            .sync(accountId = accountId, feedId = feedId, groupId = groupId)
            .let { result ->
                // 达到重试上限后放弃本轮并返回 failure：
                // 周期任务会被 WorkManager 重置（resetPeriodic），
                // 下一个同步间隔照常运行，而不是陷入无限指数退避
                if (result is Result.Retry && runAttemptCount >= MAX_RETRY_ATTEMPTS) {
                    Timber.e("Sync failed after $runAttemptCount attempts, giving up this round")
                    Result.failure()
                } else result
            }
            .let { result ->
                if (result is Result.Success) {
                    runCatching { doPostSync(service, accountId) }
                        .onFailure { Timber.e(it, "Failed to run post-sync tasks") }
                }
                result
            }
    }

    private suspend fun doPostSync(service: AbstractRssRepository, accountId: Int) {
        service.clearKeepArchivedArticles(accountId).forEach {
            readerCacheHelper.deleteCacheFor(articleId = it.id)
        }
        workManager
            .beginUniqueWork(
                uniqueWorkName = POST_SYNC_WORK_NAME,
                existingWorkPolicy = ExistingWorkPolicy.KEEP,
                OneTimeWorkRequestBuilder<ReaderWorker>()
                    .addTag(READER_TAG)
                    .addTag(ONETIME_WORK_TAG)
                    .setInputData(workDataOf("accountId" to accountId))
                    .setBackoffCriteria(
                        backoffPolicy = BackoffPolicy.EXPONENTIAL,
                        backoffDelay = 30,
                        timeUnit = TimeUnit.SECONDS,
                    )
                    .build(),
            )
            .then(OneTimeWorkRequestBuilder<WidgetUpdateWorker>().build())
            .enqueue()
    }

    companion object {
        private const val SYNC_WORK_NAME_PERIODIC = "ReadYou"

        // 最大退避重试次数（首次运行 runAttemptCount 为 0，
        // 之后每次退避重试 +1），超过后本轮放弃，等待下个周期
        const val MAX_RETRY_ATTEMPTS = 2
        @Deprecated("do not use")
        private const val READER_WORK_NAME_PERIODIC = "FETCH_FULL_CONTENT_PERIODIC"
        private const val POST_SYNC_WORK_NAME = "POST_SYNC_WORK"

        private const val SYNC_ONETIME_NAME = "SYNC_ONETIME"

        const val SYNC_TAG = "SYNC_TAG"
        const val READER_TAG = "READER_TAG"
        const val ONETIME_WORK_TAG = "ONETIME_WORK_TAG"
        const val PERIODIC_WORK_TAG = "PERIODIC_WORK_TAG"

        fun cancelOneTimeWork(workManager: WorkManager) {
            workManager.cancelUniqueWork(SYNC_ONETIME_NAME)
        }

        fun cancelPeriodicWork(workManager: WorkManager) {
            workManager.cancelUniqueWork(SYNC_WORK_NAME_PERIODIC)
            workManager.cancelUniqueWork(READER_WORK_NAME_PERIODIC)
        }

        fun enqueueOneTimeWork(workManager: WorkManager, inputData: Data = workDataOf()) {
            workManager
                .beginUniqueWork(
                    SYNC_ONETIME_NAME,
                    ExistingWorkPolicy.REPLACE,
                    OneTimeWorkRequestBuilder<SyncWorker>()
                        .addTag(SYNC_TAG)
                        .addTag(ONETIME_WORK_TAG)
                        .setInputData(inputData)
                        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                        .build(),
                )
                .enqueue()
        }

        fun enqueuePeriodicWork(account: Account, workManager: WorkManager) {
            workManager.enqueueUniquePeriodicWork(
                SYNC_WORK_NAME_PERIODIC,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                buildPeriodicWorkRequest(account),
            )
            workManager.cancelUniqueWork(READER_WORK_NAME_PERIODIC)
        }

        private fun buildPeriodicWorkRequest(
            account: Account,
        ): PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<SyncWorker>(
                account.syncInterval.value,
                TimeUnit.MINUTES,
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresCharging(account.syncOnlyWhenCharging.value)
                        .setRequiredNetworkType(
                            if (account.syncOnlyOnWiFi.value) NetworkType.UNMETERED
                            else NetworkType.CONNECTED
                        )
                        .build()
                )
                .setBackoffCriteria(
                    backoffPolicy = BackoffPolicy.EXPONENTIAL,
                    backoffDelay = 30,
                    timeUnit = TimeUnit.SECONDS,
                )
                .setInputData(workDataOf("accountId" to account.id))
                .addTag(SYNC_TAG)
                .addTag(PERIODIC_WORK_TAG)
                .setInitialDelay(0, TimeUnit.MINUTES)
                .build()
    }
}
