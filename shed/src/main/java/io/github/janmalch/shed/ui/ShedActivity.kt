package io.github.janmalch.shed.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.i18n.DateTimeFormatter
import androidx.core.i18n.DateTimeFormatterSkeletonOptions
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import io.github.janmalch.shed.R
import io.github.janmalch.shed.Shed
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

@SuppressLint("LogNotTimber")
internal class ShedActivity : ComponentActivity() {

    private val clock = Clock.System
    private val formatter by lazy {
        DateTimeFormatter(
            context = this,
            options = DateTimeFormatterSkeletonOptions.Builder()
                .setWeekDay(DateTimeFormatterSkeletonOptions.WeekDay.ABBREVIATED)
                .setDay(DateTimeFormatterSkeletonOptions.Day.NUMERIC)
                .setMonth(DateTimeFormatterSkeletonOptions.Month.NUMERIC)
                .setYear(DateTimeFormatterSkeletonOptions.Year.NUMERIC)
                .setHour(DateTimeFormatterSkeletonOptions.Hour.NUMERIC)
                .setMinute(DateTimeFormatterSkeletonOptions.Minute.TWO_DIGITS)
                .setSecond(DateTimeFormatterSkeletonOptions.Second.TWO_DIGITS)
                .build()
        )
    }
    private val viewModel: ShedViewModel by viewModels { ShedViewModel.Factory }
    private val shareExport = registerForActivityResult(ShareExport()) { _ ->
        lifecycleScope.launch(CoroutineExceptionHandler { _, throwable ->
            Log.w(Shed.TAG, "Failed to clean up cache directory after share.", throwable)
        }) {
            viewModel.clearCache()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
                                var isConfirmDialogVisible by rememberSaveable {
                                    mutableStateOf(
                                        false
                                    )
                                }
                                IconButton(onClick = { isConfirmDialogVisible = true }) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = stringResource(R.string.delete_logs)
                                    )
                                }
                                IconButton(onClick = { shareDumpAsJson() }) {
                                    Icon(
                                        Icons.Filled.Share,
                                        contentDescription = stringResource(R.string.share_log_file)
                                    )
                                }
                                if (isConfirmDialogVisible) {
                                    var maxAgeStr by rememberSaveable { mutableStateOf("0") }
                                    val maxAge = remember(maxAgeStr) {
                                        maxAgeStr.toIntOrNull(10)?.takeIf { it >= 0 }?.hours
                                    }
                                    val deleteBefore = remember(maxAge) {
                                        val amount = maxAge ?: return@remember "?"
                                        formatter.format((clock.now() - amount).toEpochMilliseconds())
                                    }
                                    AlertDialog(
                                        onDismissRequest = {
                                            isConfirmDialogVisible = false
                                        },
                                        icon = {
                                            Icon(Icons.Filled.Delete, contentDescription = null)
                                        },
                                        title = {
                                            Text(stringResource(R.string.delete_logs))
                                        },
                                        text = {
                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                                modifier = Modifier.fillMaxWidth(),
                                            ) {
                                                Text(stringResource(R.string.delete_logs_reassurance))
                                                TextField(
                                                    value = maxAgeStr,
                                                    onValueChange = { maxAgeStr = it },
                                                    label = {
                                                        Text(stringResource(R.string.delete_logs_max_age))
                                                    },
                                                    supportingText = {
                                                        Text(
                                                            text = when (maxAge) {
                                                                null -> stringResource(R.string.delete_logs_max_age_hint_invalid)
                                                                Duration.ZERO -> stringResource(R.string.delete_logs_max_age_hint_zero)
                                                                else -> stringResource(
                                                                    R.string.delete_logs_max_age_hint_gt_zero,
                                                                    deleteBefore
                                                                )
                                                            }
                                                        )
                                                    },
                                                    singleLine = true,
                                                    isError = maxAge == null,
                                                    modifier = Modifier.fillMaxWidth(),
                                                )
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = {
                                                isConfirmDialogVisible = false
                                            }) {
                                                Text(stringResource(R.string.cancel))
                                            }
                                        },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                val duration = maxAge ?: return@TextButton
                                                viewModel.deleteLogs(duration)
                                                isConfirmDialogVisible = false
                                            }) {
                                                Text(stringResource(R.string.confirm))
                                            }
                                        },
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
                        val priorities by viewModel.priorities.collectAsStateWithLifecycle()
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            stickyHeader(key = "priorities", contentType = "priorities") {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                        .background(MaterialTheme.colorScheme.surface)
                                        .padding(horizontal = 16.dp)
                                ) {
                                    LevelFilterChip(
                                        selected = priorities,
                                        onSelectionChange = { viewModel.setSelectedPriorities(it) }
                                    )
                                }
                            }

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
            Log.e(Shed.TAG, "Error while sharing log as JSON.", throwable)
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
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(
                this,
                getString(R.string.an_unknown_error_occurred),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
