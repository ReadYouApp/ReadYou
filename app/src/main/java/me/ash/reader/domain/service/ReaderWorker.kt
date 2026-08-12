package me.ash.reader.domain.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import me.ash.reader.infrastructure.rss.ReaderCacheHelper

@HiltWorker
class ReaderWorker
@AssistedInject
constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val rssService: RssService,
    private val cacheHelper: ReaderCacheHelper,
    private val accountService: AccountService,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val accountId = inputData.getInt("accountId", -1)
        val accountTypeId = if (accountId != -1) {
            accountService.getAccountById(accountId)?.type?.id ?: return Result.failure()
        } else {
            // 兼容未传 accountId 的旧调用方（例如已入队的旧任务）
            accountService.getCurrentAccount().type.id
        }

        val semaphore = Semaphore(2)

        val deferredList =
            withContext(Dispatchers.IO) {
                val repo = rssService.get(accountTypeId)
                val articleList = repo.queryUnreadFullContentArticles()
                articleList.map {
                    async { semaphore.withPermit { cacheHelper.checkOrFetchFullContent(it) } }
                }
            }

        return when {
            deferredList.awaitAll().all { it } -> Result.success()
            // 达到重试上限后返回 success 放弃本轮，
            // 让 POST_SYNC_WORK 唯一链正常结束（KEEP 策略下不阻塞后续同步的新链），
            // 缺失的全文会在下次同步后的新链中再次尝试抓取
            runAttemptCount >= SyncWorker.MAX_RETRY_ATTEMPTS -> Result.success()
            else -> Result.retry()
        }
    }
}
