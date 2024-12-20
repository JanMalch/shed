package io.github.janmalch.shed.tree

import android.content.Context
import io.github.janmalch.shed.database.ShedDatabase
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration

object Shed {
    fun createTree(
        context: Context,
        entryMaxAge: Duration? = null,
        keepLatest: Long? = null,
        includeStackTraces: Boolean = true,
        clock: Clock = Clock.System,
        scope: CoroutineScope = CoroutineScope(
            Dispatchers.Default +
                    SupervisorJob() +
                    CoroutineName(SHED_TAG)
        )
    ): ShedTree {
        val db = ShedDatabase.getInstance(
            context = context,
            entryMaxAge = entryMaxAge,
            keepLatest = keepLatest,
            now = clock::now,
        )
        return ShedTree(
            dao = db.logDao(),
            includeStackTraces = includeStackTraces,
            clock = clock,
            scope = scope,
        )
    }
}