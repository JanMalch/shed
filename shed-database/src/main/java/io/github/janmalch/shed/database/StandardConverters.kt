package io.github.janmalch.shed.database

import androidx.room.TypeConverter
import kotlinx.datetime.Instant

internal class StandardConverters {
    @TypeConverter
    fun toInstant(value: Long?): Instant? = value?.let { Instant.fromEpochMilliseconds(it) }

    @TypeConverter
    fun fromInstant(value: Instant?): Long? = value?.toEpochMilliseconds()
}