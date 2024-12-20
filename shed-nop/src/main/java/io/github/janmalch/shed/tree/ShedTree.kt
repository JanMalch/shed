package io.github.janmalch.shed.tree

import timber.log.Timber

class ShedTree internal constructor() : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // nop
    }

}