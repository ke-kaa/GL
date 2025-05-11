package com.example.greenleaf.data.local.dao

import androidx.room.*
import com.example.greenleaf.data.local.entities.ObservationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ObservationDao {

    @Query("SELECT * FROM observations")
    suspend fun getAll(): List<ObservationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(observation: ObservationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(observations: List<ObservationEntity>)

    @Update
    suspend fun update(observation: ObservationEntity)

    @Delete
    suspend fun delete(observation: ObservationEntity)

    @Query("DELETE FROM observations WHERE id = :id")
    suspend fun deleteById(id: Int)

    // Sync-specific
    @Query("SELECT * FROM observations WHERE isSynced = 0")
    suspend fun getUnsynced(): List<ObservationEntity>

    @Query("UPDATE observations SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Int)
}