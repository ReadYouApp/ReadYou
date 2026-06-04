package me.ash.reader.infrastructure.rss

import android.content.Context
import androidx.annotation.CheckResult
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileNotFoundException
import java.security.MessageDigest
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import me.ash.reader.domain.model.article.Article
import me.ash.reader.domain.service.AccountService
import me.ash.reader.infrastructure.di.IODispatcher

class ReaderCacheHelper
@Inject
constructor(
    @ApplicationContext private val context: Context,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val rssHelper: RssHelper,
    private val accountService: AccountService,
) {
    private val cacheDir = context.cacheDir.resolve("readability")
    private val md = MessageDigest.getInstance("SHA-256")

    private val currentCacheDir: File
        get() = cacheDir.resolve(accountService.getCurrentAccountId().toString())

    @OptIn(ExperimentalStdlibApi::class)
    private fun getFileNameFor(articleId: String): String {
        val bytes = articleId.toByteArray()
        val digest = md.digest(bytes)
        return digest.toHexString() + ".html"
    }

    private suspend fun writeContentToCache(content: String, articleId: String): Boolean {
        return withContext(ioDispatcher) {
            runCatching {
                    currentCacheDir.run {
                        mkdirs()
                        resolve(getFileNameFor(articleId)).run {
                            createNewFile()
                            writeText(content)
                        }
                    }
                }
                .fold(onSuccess = { true }, onFailure = { false })
        }
    }

    @CheckResult
    suspend fun readFullContent(articleId: String): Result<String> {
        return withContext(ioDispatcher) {
            runCatching {
                val file = currentCacheDir.resolve(getFileNameFor(articleId))
                if (!file.exists()) return@withContext Result.failure(FileNotFoundException())
                file.readText()
            }
        }
    }

    private suspend fun fetchFullContentInternal(article: Article): Result<String> {
        return withContext(ioDispatcher) {
            runCatching {
                val fullContent = rssHelper.parseFullContent(article.link, article.title)
                if (fullContent.isNotBlank()) {
                    writeContentToCache(fullContent, article.id)
                    fullContent
                } else return@withContext Result.failure(Exception())
            }
        }
    }

    @CheckResult
    suspend fun readOrFetchFullContent(article: Article): Result<String> {
        return withContext(ioDispatcher) {
            runCatching {
                val result = readFullContent(article.id)
                if (result.isSuccess) return@withContext result
                return@withContext fetchFullContentInternal(article)
            }
        }
    }

    suspend fun checkOrFetchFullContent(article: Article): Boolean {
        return withContext(ioDispatcher) {
            val file = currentCacheDir.resolve(getFileNameFor(article.id))
            try {
                if (!file.exists()) {
                    return@withContext fetchFullContentInternal(article)
                        .fold(onFailure = { false }, onSuccess = { true })
                } else {
                    return@withContext true
                }
            } catch (_: SecurityException) {
                return@withContext false
            }
        }
    }

    suspend fun deleteCacheFor(articleId: String): Boolean {
        return withContext(ioDispatcher) {
            runCatching {
                    val file = currentCacheDir.resolve(getFileNameFor(articleId))
                    if (!file.exists()) return@runCatching false
                    return@runCatching file.delete()
                }
                .fold(onSuccess = { true }, onFailure = { false })
        }
    }

    suspend fun clearCache(): Boolean {
        return withContext(ioDispatcher) {
            runCatching {
                    return@withContext currentCacheDir.deleteRecursively()
                }
                .fold(onSuccess = { true }, onFailure = { false })
        }
    }
}
