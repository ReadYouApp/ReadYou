package me.ash.reader.infrastructure.android

import android.content.Context
import android.content.Intent
import android.os.Looper
import android.util.Log
import me.ash.reader.infrastructure.exception.BusinessException
import me.ash.reader.ui.ext.showToastLong
import java.lang.Thread.UncaughtExceptionHandler

/**
 * The uncaught exception handler for the application.
 */
class CrashHandler(private val context: Context) : UncaughtExceptionHandler {

    init {
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    /**
     * Catch all uncaught exception and log it.
     */
    override fun uncaughtException(p0: Thread, p1: Throwable) {
        val causeMessage = getCauseMessage(p1)
        Log.e("RLog", "uncaughtException: $causeMessage", p1)

        when (p1) {
            is BusinessException -> {
                context.startActivity(Intent(context, CrashReportActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra(CrashReportActivity.ERROR_REPORT_KEY, p1.stackTraceToString())
                })
            }

            else -> {
                context.startActivity(Intent(context, CrashReportActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra(CrashReportActivity.ERROR_REPORT_KEY, p1.stackTraceToString())
                })
            }
        }
    }

    private fun getCauseMessage(e: Throwable?): String? {
        val cause = getCauseRecursively(e)
        return if (cause != null) cause.message.toString() else e?.javaClass?.name
    }

    private fun getCauseRecursively(e: Throwable?): Throwable? {
        var cause: Throwable?
        cause = e
        while (cause?.cause != null && cause !is RuntimeException) {
            cause = cause.cause
        }
        return cause
    }
}
