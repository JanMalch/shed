package io.github.janmalch.shed.tree

import android.annotation.SuppressLint
import android.util.Log
import io.github.janmalch.shed.database.LogDao
import io.github.janmalch.shed.database.LogEntity
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import timber.log.Timber

internal const val SHED_TAG = "ShedTree"

class ShedTree internal constructor(
    private val dao: LogDao,
    private val includeStackTraces: Boolean,
    private val clock: Clock,
    private val scope: CoroutineScope
) : Timber.DebugTree() {

    @SuppressLint("LogNotTimber")
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val now = clock.now()
        scope.launch(CoroutineExceptionHandler { _, throwable ->
            Log.e(SHED_TAG, "Error while adding log to shed database.", throwable)
        }) {
            val messageWithoutStackTrace = when (t) {
                null -> message
                else -> message.replace(t.stackTraceToString(), "").trimEnd()
            }
            dao.insert(
                LogEntity(
                    id = 0,
                    timestamp = now,
                    tag = tag,
                    priority = priority,
                    message = messageWithoutStackTrace,
                    stackTrace = t?.takeIf { includeStackTraces }?.stackTraceToString(),
                )
            )
        }
    }

}