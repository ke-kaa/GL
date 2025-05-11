package com.example.greenleaf.data.remote.models

import com.example.greenleaf.data.local.entities.ObservationEntity
import com.google.gson.annotations.SerializedName

data class ObservationRequest(
    @SerializedName("related_plant_id")
    val relatedPlantId: Int?, // Nullable, can be null

    @SerializedName("observation_image")
    val observationImage: String?, // Nullable

    val time: String, // Format: "HH:mm:ss"
    val date: String, // Format: "YYYY-MM-DD"
    val location: String,
    val note: String?,
)



data class ObservationResponse(
    val id: Int,

    @SerializedName("related_plant")
    val relatedPlant: PlantResponse?, // Nullable, can be null

    @SerializedName("observation_image")
    val observationImage: String?, // Nullable

    val time: String, // Format: "HH:mm:ss"
    val date: String, // Format: "YYYY-MM-DD"
    val location: String,
    val note: String?,

    @SerializedName("created_by")
    val createdBy: Int
)


fun ObservationResponse.toEntity(): ObservationEntity = ObservationEntity(
    id = this.id,
    relatedPlantId = this.relatedPlant?.id,
    observationImage = this.observationImage,
    time = this.time,
    date = this.date,
    location = this.location,
    note = this.note,
    isSynced = true // fetched from backend, so synced
)