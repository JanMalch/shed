package io.github.janmalch.shed.tree

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.Clock
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration

object Shed {
    fun createTree(
        context: Context,
        entryMaxAge: Duration? = null,
        keepLatest: Long? = null,
        includeStackTraces: Boolean = true,
        clock: Clock = Clock.System,
        scope: CoroutineScope = CoroutineScope(EmptyCoroutineContext)
    ): ShedTree {
        return ShedTree()
    }
}