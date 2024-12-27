package com.github.janmalch.shed.ui

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
import com.github.janmalch.shed.Shed
import com.github.janmalch.shed.database.LogDao
import com.github.janmalch.shed.database.ShedDatabase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import java.io.File

@SuppressLint("LogNotTimber")
internal class ShedViewModel(
    private val dao: LogDao,
    private val cacheDir: File,
    private val clock: Clock,
    private val json: Json = Json { prettyPrint = true },
) : ViewModel() {

    val totalEntries = dao.countAll()
        .catch {
            Log.e(Shed.TAG, "Error while counting all log entries.", it)
            emit(-1)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = -1,
        )

    val logsFlow = Pager(
        config = PagingConfig(pageSize = 20),
        pagingSourceFactory = { dao.pagingSource() }
    ).flow
        .cachedIn(viewModelScope)

    /**
     * @return the temporary file or `null` if log is empty
     */
    @OptIn(ExperimentalSerializationApi::class)
    @Throws(Exception::class)
    suspend fun dumpToFile(): File? {
        val entireLog = dao.findAll()
        if (entireLog.isEmpty()) {
            return null
        }
        val tmp = File(
            cacheDir.also { it.mkdir() },
            "log-${clock.now().toEpochMilliseconds()}.json",
        )
        tmp.outputStream().buffered().use { out ->
            json.encodeToStream(entireLog, out)
        }
        return tmp
    }

    @Throws(Exception::class)
    fun clearCache() {
        cacheDir.delete()
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
                    cacheDir = File(application.cacheDir, "shed"),
                    clock = Clock.System
                )
            }
        }
    }
}