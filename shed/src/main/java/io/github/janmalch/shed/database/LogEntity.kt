package io.github.janmalch.shed.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Instant
import kotlinx.serialization.Serializable

internal const val logTableName = "logs"
internal const val idColName = "id"
internal const val timestampColName = "timestamp"

@Serializable
@Entity(tableName = logTableName)
internal data class LogEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = idColName) val id: Int,
    @ColumnInfo(name = timestampColName) val timestamp: Instant,
    @ColumnInfo(name = "tag") val tag: String?,
    @ColumnInfo(name = "priority") val priority: Int,
    @ColumnInfo(name = "message") val message: String,
    @ColumnInfo(name = "stackTrace") val stackTrace: String?,
)

