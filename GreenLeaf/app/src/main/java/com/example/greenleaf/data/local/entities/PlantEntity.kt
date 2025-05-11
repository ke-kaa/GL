package com.example.greenleaf.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plants")
data class PlantEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val plantImage: String?,
    val commonName: String,
    val scientificName: String,
    val habitat: String,
    val origin: String?,
    val description: String?,
    val isSynced: Boolean = true,
    // val createdBy: Int,
    // val lastModified: Long = System.currentTimeMillis()
) 