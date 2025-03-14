package io.github.janmalch.shed.autoload

import android.annotation.SuppressLint
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.util.Log
import io.github.janmalch.shed.Shed
import timber.log.Timber
import kotlin.time.Duration.Companion.days

internal class ShedAutoloadProvider : ContentProvider() {
    // Based on androidx.startup
    // https://github.com/androidx/androidx/blob/androidx-main/startup/startup-runtime/src/main/java/androidx/startup/InitializationProvider.java

    @SuppressLint("LogNotTimber")
    override fun onCreate(): Boolean {
        val context =
            checkNotNull(context?.applicationContext) { "Application context cannot be null." }
        Timber.plant(Shed.createTree(context = context, entryMaxAge = 3.days))
        Log.d("Shed", "Planted Timber tree with entry max age of 3 days.")
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        throw IllegalStateException("Not allowed.")
    }

    override fun getType(uri: Uri): String? {
        throw IllegalStateException("Not allowed.")
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw IllegalStateException("Not allowed.")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        throw IllegalStateException("Not allowed.")
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        throw IllegalStateException("Not allowed.")
    }

}