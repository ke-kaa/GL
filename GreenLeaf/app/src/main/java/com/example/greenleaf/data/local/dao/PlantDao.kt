package com.example.greenleaf.data.local.dao

import androidx.room.*
import com.example.greenleaf.data.local.entities.PlantEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface PlantDao {

    @Query("SELECT * FROM plants")
    suspend fun getAll(): List<PlantEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plant: PlantEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(plants: List<PlantEntity>)

    @Update
    suspend fun update(plant: PlantEntity)

    @Delete
    suspend fun delete(plant: PlantEntity)

    @Query("DELETE FROM plants WHERE id = :id")
    suspend fun deleteById(id: Int)

    // Sync-specific
    @Query("SELECT * FROM plants WHERE isSynced = 0")
    suspend fun getUnsynced(): List<PlantEntity>

    @Query("UPDATE plants SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Int)
}
