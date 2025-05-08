package com.example.greenleaf.data.remote.models

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
