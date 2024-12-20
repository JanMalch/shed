package io.github.janmalch.shed

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.format.DateFormat
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import io.github.janmalch.shed.database.LogEntity
import io.github.janmalch.shed.database.ShedDatabase
import io.github.janmalch.shed.ui.theme.ShedTheme
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.DateTimeFormatBuilder
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import java.io.File
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class ShedActivity : ComponentActivity() {

    private val viewModel: ShedViewModel by viewModels { ShedViewModel.Factory }
    private val shareExport = registerForActivityResult(ShareExport()) { _ ->
        try {
            viewModel.clearCache()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to clean up cache directory after share.", e)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ShedTheme {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = stringResource(R.string.back)
                                    )
                                }
                            },
                            title = {
                                val total by viewModel.totalEntries.collectAsStateWithLifecycle()
                                Text(
                                    text = when {
                                        total < 0 -> stringResource(R.string.logs)
                                        else -> pluralStringResource(
                                            R.plurals.num_of_logs,
                                            total,
                                            total
                                        )
                                    }
                                )
                            },
                            actions = {
                                IconButton(onClick = { viewModel.deleteAllLogs() }) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = stringResource(R.string.delete_all_logs)
                                    )
                                }
                                IconButton(onClick = { shareDumpAsJson() }) {
                                    Icon(
                                        Icons.Filled.Share,
                                        contentDescription = stringResource(R.string.share_log_file)
                                    )
                                }
                            }
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        val pagingItems = viewModel.logsFlow.collectAsLazyPagingItems()
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(
                                count = pagingItems.itemCount,
                                key = pagingItems.itemKey { it.id },
                                contentType = pagingItems.itemContentType { "log-entry" }
                            ) { index ->
                                pagingItems[index]?.let { LogEntry(it) }
                            }

                            when (pagingItems.loadState.append) {
                                LoadState.Loading -> {
                                    item {
                                        CircularProgressIndicator(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .wrapContentWidth(Alignment.CenterHorizontally)
                                        )
                                    }
                                }

                                is LoadState.NotLoading -> {
                                    if (pagingItems.loadState.append.endOfPaginationReached && pagingItems.itemCount == 0) {
                                        item(key = "no-logs", contentType = "no-logs") {
                                            Text(
                                                text = stringResource(R.string.no_log_entries_yet),
                                                fontStyle = FontStyle.Italic,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                            )
                                        }
                                    }
                                }

                                is LoadState.Error -> {
                                    item(key = "error", contentType = "error") {
                                        Text(
                                            text = stringResource(R.string.an_unknown_error_occurred),
                                            fontStyle = FontStyle.Italic,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun shareDumpAsJson() {
        lifecycleScope.launch(Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, "Error while sharing log as JSON.", throwable)
            notifyOfError()
        }) {
            val tmp = viewModel.dumpToFile() ?: return@launch notifyOfEmptyLog()
            if (isActive) {
                shareExport.launch(tmp)
            } else {
                viewModel.clearCache()
                ensureActive()
            }
        }
    }

    private fun notifyOfEmptyLog() {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(
                this,
                getString(R.string.no_log_entries_to_share),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun notifyOfError() {
        Toast.makeText(
            this,
            getString(R.string.an_unknown_error_occurred),
            Toast.LENGTH_SHORT
        ).show()
    }

    companion object {
        @JvmStatic
        @JvmName("start")
        fun start(context: Context) {
            context.startActivity(Intent(context, ShedActivity::class.java))
        }
    }
}
