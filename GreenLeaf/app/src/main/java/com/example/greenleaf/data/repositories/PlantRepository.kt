package com.example.greenleaf.data.repositories

import com.example.greenleaf.data.local.dao.PlantDao
import com.example.greenleaf.data.local.entities.PlantEntity
import com.example.greenleaf.data.remote.api.GreenLeafApi
import com.example.greenleaf.data.remote.models.PlantResponse
import com.example.greenleaf.data.remote.models.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlantRepository @Inject constructor(
    private val plantDao: PlantDao,
    private val api: GreenLeafApi
) {
    suspend fun fetchAndCachePlants() {
        val response = api.getPlants() // Returns Response<List<PlantResponse>>
        if (response.isSuccessful) {
            val remotePlants = response.body()?.map { plantResponse: PlantResponse -> plantResponse.toEntity() } ?: emptyList()
            plantDao.insertAll(remotePlants)
        }
    }

    suspend fun getPlants(): List<PlantEntity> = withContext(Dispatchers.IO) {
        plantDao.getAll()
    }

    suspend fun addPlant(plant: PlantEntity) {
        plantDao.insert(plant.copy(isSynced = false))
    }

    suspend fun updatePlant(plant: PlantEntity) {
        plantDao.update(plant.copy(isSynced = false))
    }

    suspend fun deletePlant(plant: PlantEntity) {
        plantDao.delete(plant)
    }

    suspend fun syncUnsyncedPlants() {
        val unsynced = plantDao.getUnsynced()
        for (plant in unsynced) {
            try {
                val commonNameBody = RequestBody.create("text/plain".toMediaTypeOrNull(), plant.commonName)
                val scientificNameBody = RequestBody.create("text/plain".toMediaTypeOrNull(), plant.scientificName)
                val habitatBody = RequestBody.create("text/plain".toMediaTypeOrNull(), plant.habitat)
                val originBody = plant.origin?.let { RequestBody.create("text/plain".toMediaTypeOrNull(), it) }
                val descriptionBody = plant.description?.let { RequestBody.create("text/plain".toMediaTypeOrNull(), it) }
                val imagePart = plant.plantImage?.let { imagePath ->
                    val file = java.io.File(imagePath)
                    val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                    MultipartBody.Part.createFormData("plant_image", file.name, requestFile)
                }
                api.createPlantMultipart(commonNameBody, scientificNameBody, habitatBody, originBody, descriptionBody, imagePart)
                plantDao.markAsSynced(plant.id)
            } catch (e: Exception) {
                // Handle error, keep isSynced = false
            }
        }
    }
}
