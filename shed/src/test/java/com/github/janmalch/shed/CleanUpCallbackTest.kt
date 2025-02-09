package io.github.janmalch.shed

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import io.github.janmalch.shed.database.CleanUpCallback
import io.github.janmalch.shed.database.LogDao
import io.github.janmalch.shed.database.LogEntity
import io.github.janmalch.shed.database.ShedDatabase
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.After
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.runner.RunWith

import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Duration.Companion.seconds

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class CleanUpCallbackTest {

    private lateinit var shedDb: ShedDatabase
    private lateinit var sqliteDb: SupportSQLiteDatabase
    private lateinit var dao: LogDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        shedDb = Room.inMemoryDatabaseBuilder(context, ShedDatabase::class.java)
            .build()
        dao = shedDb.logDao()
        sqliteDb = shedDb.openHelper.writableDatabase

        runBlocking {
            repeat(10) {
                dao.insert(
                    LogEntity(
                        id = 0,
                        timestamp = Instant.fromEpochSeconds((it + 1).toLong(), 0),
                        tag = null,
                        priority = 1,
                        message = "Log #${it + 1}",
                        stackTrace = null,
                    )
                )
            }
        }
    }

    @After
    fun tearDown() {
        shedDb.close()
    }

    @Test
    fun deleteOldEntries_deletes_correctly() = runBlocking {
        verifySetup()
        CleanUpCallback.deleteOldEntries(
            db = sqliteDb,
            entryMaxAge = 5.seconds,
            clock = object : Clock {
                override fun now(): Instant = Instant.fromEpochMilliseconds(10_000)
            }
        )
        assertEquals(
            (10 downTo 5).toList(),
            dao.findAll().map { it.id },
        )
    }

    @Test
    fun keepLatestEntries_deletes_correctly() = runBlocking {
        verifySetup()
        CleanUpCallback.keepLatestEntries(
            db = sqliteDb,
            keepLatest = 5,
        )
        assertEquals((10 downTo 6).toList(), dao.findAll().map { it.id })
    }

    private suspend fun verifySetup() {
        assertEquals(
            "Test setup invalid.",
            (10 downTo 1).toList(),
            dao.findAll().map { it.id }
        )
    }
}