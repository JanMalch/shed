package io.github.janmalch.shed.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface LogDao {
    @Query("SELECT COUNT(1) FROM $logTableName")
    fun countAll(): Flow<Int>

    @Query("SELECT COUNT(1) FROM $logTableName WHERE priority IN (:priorities)")
    fun countAll(priorities: Set<Int>): Flow<Int>

    @Query("SELECT * FROM $logTableName ORDER BY timestamp DESC")
    suspend fun findAll(): List<LogEntity>

    @Query("SELECT * FROM $logTableName ORDER BY timestamp DESC")
    fun pagingSource(): PagingSource<Int, LogEntity>

    @Query("SELECT * FROM $logTableName WHERE priority IN (:priorities) ORDER BY timestamp DESC")
    fun pagingSource(priorities: Set<Int>): PagingSource<Int, LogEntity>

    @Insert
    suspend fun insert(log: LogEntity)

    @Query("DELETE FROM $logTableName")
    suspend fun clear()
}
