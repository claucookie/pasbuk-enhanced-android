package labs.claucookie.pasbuk.util

import android.content.Context
import android.content.Intent
import android.util.Log
import kotlin.system.exitProcess

/**
 * Global exception handler for catching uncaught exceptions.
 *
 * This handler:
 * - Logs the exception details
 * - Shows a user-friendly error screen
 * - Prevents the app from crashing silently
 */
class GlobalExceptionHandler private constructor(
    private val applicationContext: Context,
    private val defaultHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, exception: Throwable) {
        try {
            // Log the exception
            Log.e(TAG, "Uncaught exception in thread ${thread.name}", exception)

            // Launch error activity
            launchErrorActivity(exception)

            // Give the error activity time to start
            Thread.sleep(ERROR_ACTIVITY_LAUNCH_DELAY)
        } catch (e: Exception) {
            // If we can't handle the error, fall back to default handler
            Log.e(TAG, "Error in exception handler", e)
        } finally {
            // Call default handler if it exists, then exit
            defaultHandler?.uncaughtException(thread, exception)
            exitProcess(1)
        }
    }

    private fun launchErrorActivity(exception: Throwable) {
        val intent = Intent(applicationContext, ErrorActivity::class.java).apply {
            putExtra(EXTRA_ERROR_MESSAGE, exception.message ?: "An unexpected error occurred")
            putExtra(EXTRA_STACK_TRACE, exception.stackTraceToString())
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        applicationContext.startActivity(intent)
    }

    companion object {
        private const val TAG = "GlobalExceptionHandler"
        private const val ERROR_ACTIVITY_LAUNCH_DELAY = 100L
        const val EXTRA_ERROR_MESSAGE = "error_message"
        const val EXTRA_STACK_TRACE = "stack_trace"

        /**
         * Initialize the global exception handler.
         * Call this once in Application.onCreate()
         */
        fun initialize(context: Context) {
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            val handler = GlobalExceptionHandler(
                context.applicationContext,
                defaultHandler
            )
            Thread.setDefaultUncaughtExceptionHandler(handler)
        }
    }
}
