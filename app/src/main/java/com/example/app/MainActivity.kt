package com.example.app

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.app.ui.theme.ShedTheme
import io.github.janmalch.shed.ShedActivity
import timber.log.Timber

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShedTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth().padding(innerPadding),
                    ) {
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = {
                            Timber.v("This is an verbose log without exception.")
                        }) {
                            Text("Add Verbose Log")
                        }
                        Button(onClick = {
                            Timber.d("This is an debug log without exception.")
                        }) {
                            Text("Add Debug Log")
                        }
                        Button(onClick = {
                            Timber.i("This is an info log without exception.")
                        }) {
                            Text("Add Info Log")
                        }
                        Button(onClick = {
                            Timber.w(
                                RuntimeException("Don't be afraid!"),
                                "This is a warning with an exception.",
                            )
                        }) {
                            Text("Add Warning Log")
                        }
                        Button(onClick = {
                            Timber.e(
                                RuntimeException("Be afraid!"),
                                "This is an error with an exception.",
                            )
                        }) {
                            Text("Add Error Log")
                        }
                        Button(onClick = {
                            Timber.wtf(
                                "This is an assertion without an exception, but with a rather long message which can extend multiple lines.",
                            )
                        }) {
                            Text("Add Assert Log")
                        }
                        HorizontalDivider()
                        Button(onClick = {
                            ShedActivity.start(this@MainActivity)
                        }) {
                            Text("View Logs")
                        }
                    }
                }
            }
        }
    }
}
