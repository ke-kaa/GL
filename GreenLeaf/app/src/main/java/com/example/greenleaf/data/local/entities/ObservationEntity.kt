package com.example.greenleaf.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "observations",
    foreignKeys = [
        ForeignKey(
            entity = PlantEntity::class,
            parentColumns = ["id"],
            childColumns = ["relatedPlantId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class ObservationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val relatedPlantId: Int?,
    val observationImage: String?,
    val time: String,
    val date: String,
    val location: String,
    val note: String?,
    val isSynced: Boolean = true,
) 