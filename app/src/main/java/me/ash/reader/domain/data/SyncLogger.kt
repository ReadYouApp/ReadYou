package me.ash.reader.domain.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.ash.reader.infrastructure.di.ApplicationScope

/**
 * Manages logging throwables to a directory, ensuring the number of log files does not exceed a
 * limit.
 */
@Singleton
class SyncLogger
@Inject
constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope private val coroutineScope: CoroutineScope,
) {

    private val cacheDir = context.cacheDir
    val logDir = cacheDir.resolve("logs").resolve("sync")
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS")

    /**
     * Writes a throwable's stack trace to a file in the specified directory. If the number of log
     * files exceeds maxFiles, the oldest file is deleted.
     *
     * @param throwable The exception to log.
     */
    suspend fun log(throwable: Throwable) {
        withContext(Dispatchers.IO) {
            try {
                if (!logDir.exists()) {
                    logDir.mkdirs()
                }

                cleanupOldLogs()

                val timestamp = LocalDateTime.now().format(formatter)
                val logFile = File(logDir, "$timestamp.log")
                logFile.writeText(throwable.stackTraceToString())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun list(): List<Log> {
        return withContext(Dispatchers.IO) {
            logDir
                .walkTopDown()
                .filter { file -> file.isFile && file.name.endsWith(".log") }
                .map { Log(it.name, it.readText()) }
                .toList()
        }
    }

    suspend fun clear() =
        withContext(Dispatchers.IO) { logDir.walkTopDown().forEach { if (it.isFile) it.delete() } }

    /** Checks the number of log files and deletes the oldest if the count exceeds the limit. */
    private fun cleanupOldLogs(maxFiles: Int = 10) {
        val logFiles = logDir.listFiles { _, name -> name.endsWith(".log") } ?: return

        if (logFiles.size >= maxFiles) {
            val oldestFile = logFiles.minByOrNull { it.name }
            oldestFile?.delete()
        }
    }
}

data class Log(val fileName: String, val content: String)
