package io.github.janmalch.shed

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity

class ShedActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finish()
    }

    companion object {
        @JvmStatic
        @JvmName("start")
        fun start(context: Context) {
            // nop
        }
    }
}
