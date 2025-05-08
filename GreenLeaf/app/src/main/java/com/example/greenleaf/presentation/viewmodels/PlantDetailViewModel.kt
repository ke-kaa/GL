package com.example.greenleaf.presentation.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenleaf.data.remote.api.GreenLeafApi
import com.example.greenleaf.data.remote.models.PlantResponse
import com.example.greenleaf.fakedata.Plant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.net.UnknownHostException
import java.net.SocketTimeoutException

@HiltViewModel
class PlantDetailViewModel @Inject constructor(
    private val api: GreenLeafApi
) : ViewModel() {
    var plant by mutableStateOf<Plant?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    /** Load a single plant for details */
    fun loadPlant(id: String) {
        if (id.isBlank()) {
            error = "Invalid plant ID"
            return
        }

        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                val plantId = id.toIntOrNull()
                if (plantId == null) {
                    error = "Invalid plant ID format"
                    return@launch
                }

                val response = api.getPlantDetail(plantId)
                if (response.isSuccessful) {
                    response.body()?.let { plantResponse ->
                        try {
                            plant = plantResponse.toPlant()
                        } catch (e: Exception) {
                            error = "Error processing plant data: ${e.message}"
                        }
                    } ?: run {
                        error = "Plant not found"
                    }
                } else {
                    error = when (response.code()) {
                        404 -> "Plant not found"
                        401 -> "Unauthorized access"
                        403 -> "Access forbidden"
                        else -> "Failed to load plant: ${response.code()}"
                    }
                }
            } catch (e: UnknownHostException) {
                error = "Network error: Please check your internet connection"
            } catch (e: SocketTimeoutException) {
                error = "Connection timeout: Please try again"
            } catch (e: Exception) {
                error = "Error loading plant: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    /** Delete and clear */
    fun deletePlant(id: String) {
        if (id.isBlank()) {
            error = "Invalid plant ID"
            return
        }

        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                val plantId = id.toIntOrNull()
                if (plantId == null) {
                    error = "Invalid plant ID format"
                    return@launch
                }

                val response = api.deletePlant(plantId)
                if (response.isSuccessful) {
                    plant = null
                } else {
                    error = when (response.code()) {
                        404 -> "Plant not found"
                        401 -> "Unauthorized access"
                        403 -> "Access forbidden"
                        else -> "Failed to delete plant: ${response.code()}"
                    }
                }
            } catch (e: UnknownHostException) {
                error = "Network error: Please check your internet connection"
            } catch (e: SocketTimeoutException) {
                error = "Connection timeout: Please try again"
            } catch (e: Exception) {
                error = "Error deleting plant: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    private fun PlantResponse.toPlant(): Plant {
        return Plant(
            id = id.toString(),
            commonName = commonName.ifBlank { "Unknown" },
            scientificName = scientificName.ifBlank { "Unknown" },
            habitat = habitat.ifBlank { "Unknown" },
            origin = origin,
            description = description,
            plantImageUrl = plantImage,
            createdByUserId = createdBy.toString()
        )
    }
}
