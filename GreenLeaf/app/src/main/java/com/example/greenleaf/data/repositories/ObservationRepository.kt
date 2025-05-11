package com.example.greenleaf.data.repositories

import com.example.greenleaf.data.local.dao.ObservationDao
import com.example.greenleaf.data.local.entities.ObservationEntity
import com.example.greenleaf.data.remote.api.GreenLeafApi
import com.example.greenleaf.data.remote.models.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObservationRepository @Inject constructor(
    private val observationDao: ObservationDao,
    private val api: GreenLeafApi
) {
    suspend fun fetchAndCacheObservations() {
        val response = api.getObservations() // Should return Response<List<ObservationResponse>>
        if (response.isSuccessful) {
            val remoteObs = response.body()?.map { it.toEntity() } ?: emptyList()
            observationDao.insertAll(remoteObs)
        }
    }

    suspend fun getObservations(): List<ObservationEntity> = withContext(Dispatchers.IO) {
        observationDao.getAll()
    }

    suspend fun addObservation(obs: ObservationEntity) {
        observationDao.insert(obs.copy(isSynced = false))
    }

    suspend fun updateObservation(observation: ObservationEntity) {
        observationDao.update(observation.copy(isSynced = false))
    }

    suspend fun deleteObservation(observation: ObservationEntity) {
        observationDao.delete(observation)
    }

    suspend fun syncUnsyncedObservations() {
        val unsynced = observationDao.getUnsynced()
        for (obs in unsynced) {
            try {
                val relatedPlantIdBody = RequestBody.create("text/plain".toMediaTypeOrNull(), obs.relatedPlantId.toString())
                val dateBody = RequestBody.create("text/plain".toMediaTypeOrNull(), obs.date)
                val timeBody = RequestBody.create("text/plain".toMediaTypeOrNull(), obs.time)
                val locationBody = RequestBody.create("text/plain".toMediaTypeOrNull(), obs.location)
                val noteBody = obs.note?.let { RequestBody.create("text/plain".toMediaTypeOrNull(), it) }
                val imagePart = obs.observationImage?.let { imagePath ->
                    val file = java.io.File(imagePath)
                    val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                    MultipartBody.Part.createFormData("observation_image", file.name, requestFile)
                }
                api.createObservationMultipart(relatedPlantIdBody, dateBody, timeBody, locationBody, noteBody, imagePart)
                observationDao.markAsSynced(obs.id)
            } catch (e: Exception) {
                // Handle error, keep isSynced = false
            }
        }
    }
}
