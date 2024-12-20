package io.github.janmalch.shed.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration

@Database(entities = [LogEntity::class], version = 1, exportSchema = false)
@TypeConverters(StandardConverters::class)
abstract class ShedDatabase : RoomDatabase() {
    abstract fun logDao(): LogDao

    companion object {
        @Volatile
        private var INSTANCE: ShedDatabase? = null

        @JvmStatic
        fun getInstance(
            context: Context,
            entryMaxAge: Duration? = null,
            keepLatest: Long? = null,
            now: () -> Instant = Clock.System::now,
        ): ShedDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: run {
                    Room.databaseBuilder(context, ShedDatabase::class.java, "io.github.janmalch.shed.sheddb")
                        .addCallback(object : Callback() {
                            override fun onOpen(db: SupportSQLiteDatabase) {
                                super.onOpen(db)
                                if (entryMaxAge != null) {
                                    val minTimestamp = now() - entryMaxAge
                                    db.execSQL(
                                        "DELETE FROM $logTableName WHERE $timestampColName < ?",
                                        arrayOf(minTimestamp.toEpochMilliseconds())
                                    )
                                }
                                if (keepLatest != null) {
                                    db.execSQL(
                                        "DELETE FROM $logTableName WHERE $idColName NOT IN (" +
                                                "SELECT $idColName FROM $logTableName " +
                                                "ORDER BY $timestampColName DESC LIMIT ?" +
                                                ")",
                                        arrayOf(keepLatest)
                                    )
                                }
                            }
                        })
                        .build()
                        .also { INSTANCE = it }
                }
            }
    }
}