package io.github.janmalch.shed.database

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.datetime.Clock
import kotlin.time.Duration

// extracted to ease testing
@VisibleForTesting
internal class CleanUpCallback(
    private val entryMaxAge: Duration?,
    private val keepLatest: Long?,
    private val clock: Clock,
) : RoomDatabase.Callback() {
    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        deleteOldEntries(db, entryMaxAge, clock)
        keepLatestEntries(db, keepLatest)
    }

    companion object {
        fun deleteOldEntries(db: SupportSQLiteDatabase, entryMaxAge: Duration?, clock: Clock) {
            if (entryMaxAge == null) return
            val minTimestamp = clock.now() - entryMaxAge
            db.execSQL(
                "DELETE FROM $logTableName WHERE $timestampColName < ?",
                arrayOf(minTimestamp.toEpochMilliseconds())
            )
        }

        fun keepLatestEntries(db: SupportSQLiteDatabase, keepLatest: Long?) {
            if (keepLatest == null) return
            db.execSQL(
                "DELETE FROM $logTableName WHERE $idColName NOT IN (" +
                        "SELECT $idColName FROM $logTableName " +
                        "ORDER BY $timestampColName DESC LIMIT ?" +
                        ")",
                arrayOf(keepLatest)
            )
        }
    }
}

@Database(entities = [LogEntity::class], version = 1, exportSchema = false)
@TypeConverters(StandardConverters::class)
internal abstract class ShedDatabase : RoomDatabase() {
    abstract fun logDao(): LogDao

    companion object {
        @Volatile
        private var INSTANCE: ShedDatabase? = null

        @JvmStatic
        fun getInstance(
            context: Context,
            entryMaxAge: Duration? = null,
            keepLatest: Long? = null,
            clock: Clock = Clock.System,
        ): ShedDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: run {
                    Room.databaseBuilder(
                        context.applicationContext,
                        ShedDatabase::class.java,
                        "io.github.janmalch.shed.sheddb"
                    )
                        .addCallback(CleanUpCallback(entryMaxAge, keepLatest, clock))
                        .build()
                        .also { INSTANCE = it }
                }
            }
    }
}