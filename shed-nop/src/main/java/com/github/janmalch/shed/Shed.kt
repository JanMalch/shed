package com.github.janmalch.shed

import android.content.Context
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
        scope: CoroutineScope = CoroutineScope(EmptyCoroutineContext)
    ): Timber.Tree = ShedNopTree()
}