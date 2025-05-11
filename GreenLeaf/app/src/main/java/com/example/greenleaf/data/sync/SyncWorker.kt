package com.example.greenleaf.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.greenleaf.data.local.AppDatabase
import com.example.greenleaf.data.remote.api.GreenLeafApi
import com.example.greenleaf.data.repositories.PlantRepository
import com.example.greenleaf.data.repositories.ObservationRepository
import retrofit2.Retrofit
import javax.inject.Inject

class SyncWorker @Inject constructor(
    context: Context,
    workerParams: WorkerParameters,
    private val api: GreenLeafApi
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        val db = AppDatabase.getDatabase(applicationContext)
        val plantRepo = PlantRepository(db.plantDao(), api)
        val obsRepo = ObservationRepository(db.observationDao(), api)
        try {
            plantRepo.syncUnsyncedPlants()
            obsRepo.syncUnsyncedObservations()
            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }
}
