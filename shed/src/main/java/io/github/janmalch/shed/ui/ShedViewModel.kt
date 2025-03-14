package io.github.janmalch.shed.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import io.github.janmalch.shed.Shed
import io.github.janmalch.shed.database.LogDao
import io.github.janmalch.shed.database.ShedDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import java.io.File

internal val ALL_PRIORITIES = setOf(
    Log.VERBOSE, Log.DEBUG, Log.INFO, Log.WARN, Log.ERROR, Log.ASSERT,
)

@OptIn(ExperimentalCoroutinesApi::class)
@SuppressLint("LogNotTimber")
internal class ShedViewModel(
    private val dao: LogDao,
    // File constructor is not main-thread-safe, so take in a factory
    private val cacheDir: () -> File,
    private val clock: Clock = Clock.System,
    private val json: Json = Json { prettyPrint = true },
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    private val _priorities = MutableStateFlow(ALL_PRIORITIES)
    val priorities = _priorities.asStateFlow()

    val totalEntries = _priorities.flatMapLatest { dao.countAll(it) }
        .catch {
            Log.e(Shed.TAG, "Error while counting all log entries.", it)
            emit(-1)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = -1,
        )

    val logsFlow = _priorities.flatMapLatest {
        Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { dao.pagingSource(it) }
        ).flow
    }.cachedIn(viewModelScope)

    /**
     * @return the temporary file or `null` if log is empty
     */
    @OptIn(ExperimentalSerializationApi::class)
    @Throws(Exception::class)
    suspend fun dumpToFile(): File? = withContext(ioDispatcher) {
        val entireLog = dao.findAll()
        if (entireLog.isEmpty()) {
            return@withContext null
        }
        val tmp = File(
            cacheDir().also { it.mkdir() },
            "log-${clock.now().toEpochMilliseconds()}.json",
        )
        tmp.outputStream().buffered().use { out ->
            json.encodeToStream(entireLog, out)
        }
        return@withContext tmp
    }

    fun setSelectedPriorities(priorities: Set<Int>) {
        _priorities.value = priorities.takeUnless { it.isEmpty() } ?: ALL_PRIORITIES
    }

    @Throws(Exception::class)
    suspend fun clearCache(): Unit = withContext(ioDispatcher) {
        cacheDir().delete()
    }

    fun deleteAllLogs() {
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            Log.e(Shed.TAG, "Error while deleting all logs from database.", throwable)
        }) {
            dao.clear()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[APPLICATION_KEY])
                val dao = ShedDatabase.getInstance(application).logDao()
                ShedViewModel(
                    dao = dao,
                    cacheDir = { File(application.cacheDir, "shed") },
                )
            }
        }
    }
}