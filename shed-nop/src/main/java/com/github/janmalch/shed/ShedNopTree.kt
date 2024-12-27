package com.github.janmalch.shed

import timber.log.Timber

internal class ShedNopTree : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // nop
    }

}