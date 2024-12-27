package com.github.janmalch.shed.ui


import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.FileProvider
import java.io.File

internal class ShareExport : ActivityResultContract<File, Boolean>() {
    override fun createIntent(context: Context, input: File): Intent {
        val uri =
            FileProvider.getUriForFile(context, context.packageName + ".com.github.janmalch.shed.fileprovider", input)

        return Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                clipData = ClipData.newRawUri("", uri)
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra("EXTRA_FILE_PATH", input)
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            },
            "App Logs"
        )
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean =
        resultCode == Activity.RESULT_OK
}
