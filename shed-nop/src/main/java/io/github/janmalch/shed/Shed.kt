package io.github.janmalch.shed

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration

/**
 * Entry point for Shed, to put [Timber] logs into a Room database.
 *
 * Its functions provide no-op behaviour.
 */
object Shed {

    /**
     * A functional interface to determine whether a log entry should be persisted or not.
     *
     * ```kotlin
     * val persistWarnAndHigher = LogFilter { priority, _, _, _ -> priority >= Log.WARN }
     * ```
     *
     * @see filter
     */
    fun interface LogFilter {
        /**
         * Returns `true` if and only if the log entry should be persisted.
         * @param priority Log level. See [Log] for constants.
         * @param tag Explicit or inferred tag.
         * @param message Formatted log message.
         * @param t Accompanying exceptions.
         * @see Timber.Tree.log
         */
        fun filter(priority: Int, tag: String?, message: String, t: Throwable?): Boolean
    }

    /**
     * Does nothing and returns immediately.
     *
     * @param context unused
     */
    @JvmStatic
    fun startActivity(context: Context) {
        // no-op
    }

    /**
     * Returns a new tree which performs no logging.
     *
     * All parameters are unused and only exist for API parity.
     */
    @JvmStatic
    fun createTree(
        context: Context,
        entryMaxAge: Duration? = null,
        keepLatest: Long? = null,
        includeStackTraces: Boolean = true,
        filter: LogFilter = LogFilter { _, _, _, _ -> false },
        scope: CoroutineScope = CoroutineScope(EmptyCoroutineContext)
    ): Timber.Tree = ShedNopTree()
}