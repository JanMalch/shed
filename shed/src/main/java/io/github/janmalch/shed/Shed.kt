package io.github.janmalch.shed

import android.content.Context
import android.content.Intent
import android.util.Log
import io.github.janmalch.shed.database.ShedDatabase
import io.github.janmalch.shed.tree.ShedTree
import io.github.janmalch.shed.ui.ShedActivity
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import timber.log.Timber
import kotlin.time.Duration

/**
 * Entry point for Shed, to put [Timber] logs into a Room database.
 */
object Shed {
    internal const val TAG = "Shed"

    /**
     * A functional interface to determine whether a log entry should be persisted or not.
     *
     * ```kotlin
     * val persistWarnAndHigher = Shed.LogFilter { priority, _, _, _ -> priority >= Log.WARN }
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
     * Starts the activity to view and export stored [Timber] logs.
     *
     * @param context the context to create an [Intent] and [start the activity][Context.startActivity]
     */
    @JvmStatic
    fun startActivity(context: Context) {
        context.startActivity(Intent(context, ShedActivity::class.java))
    }

    /**
     * Returns a new tree which puts all logs into a local database.
     *
     * It supports clean-up operations, only running when opening the database.
     * They are not performed at later stages to reduce runtime overhead.
     *
     * @param context the context for database creation.
     * @param entryMaxAge maximum age for log entries.
     * Older entries will be removed when opening the database.
     * `null` disables this clean-up operation. Default is `null`.
     * @param keepLatest maximum amount of log entries to keep.
     * Old entries beyond this limit will be removed when opening the database.
     * `null` disables this clean-up operation. Default is `null`.
     * @param includeStackTraces save stack traces in database. Default is `true`.
     * @param scope the [CoroutineScope] to run the database inserts on.
     * @see androidx.room.RoomDatabase.Callback.onOpen
     */
    @JvmStatic
    fun createTree(
        context: Context,
        entryMaxAge: Duration? = null,
        keepLatest: Long? = null,
        includeStackTraces: Boolean = true,
        filter: LogFilter = LogFilter { _, _, _, _ -> true },
        scope: CoroutineScope = CoroutineScope(
            Dispatchers.Default +
                    SupervisorJob() +
                    CoroutineName(TAG)
        )
    ): Timber.Tree {
        val db = ShedDatabase.getInstance(
            context = context,
            entryMaxAge = entryMaxAge,
            keepLatest = keepLatest,
        )
        return ShedTree(
            dao = db.logDao(),
            includeStackTraces = includeStackTraces,
            filter = filter,
            scope = scope,
        )
    }
}