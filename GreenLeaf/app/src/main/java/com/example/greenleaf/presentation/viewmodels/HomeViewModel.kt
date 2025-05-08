package com.example.greenleaf.presentation.viewmodels

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenleaf.data.remote.api.GreenLeafApi
import com.example.greenleaf.data.remote.models.ObservationResponse
import com.example.greenleaf.data.remote.models.PlantResponse
import com.example.greenleaf.fakedata.Plant
import com.example.greenleaf.fakedata.Observation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.net.UnknownHostException
import java.net.SocketTimeoutException

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val api: GreenLeafApi
) : ViewModel() {
    private val _plants = mutableStateListOf<Plant>()
    private val _observations = mutableStateListOf<Observation>()
    private val _isLoading = mutableStateOf(false)
    private val _error = mutableStateOf<String?>(null)

    val plants: List<Plant> get() = _plants
    val observations: List<Observation> get() = _observations
    val isLoading: Boolean get() = _isLoading.value
    val error: String? get() = _error.value

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Fetch plants
                val plantsResponse = api.getPlants()
                if (plantsResponse.isSuccessful) {
                    _plants.clear()
                    plantsResponse.body()?.let { plantResponses ->
                        try {
                            _plants.addAll(plantResponses.mapNotNull { it.toPlant() })
                        } catch (e: Exception) {
                            _error.value = "Error processing plant data: ${e.message}"
                        }
                    }
                } else {
                    _error.value = when (plantsResponse.code()) {
                        404 -> "No plants found"
                        401 -> "Unauthorized access"
                        403 -> "Access forbidden"
                        else -> "Failed to load plants: ${plantsResponse.code()}"
                    }
                }

                // Fetch observations
                val observationsResponse = api.getObservations()
                if (observationsResponse.isSuccessful) {
                    _observations.clear()
                    observationsResponse.body()?.let { observationResponses ->
                        try {
                            _observations.addAll(observationResponses.mapNotNull { it.toObservation() })
                        } catch (e: Exception) {
                            _error.value = "Error processing observation data: ${e.message}"
                        }
                    }
                } else {
                    _error.value = when (observationsResponse.code()) {
                        404 -> "No observations found"
                        401 -> "Unauthorized access"
                        403 -> "Access forbidden"
                        else -> "Failed to load observations: ${observationsResponse.code()}"
                    }
                }
            } catch (e: UnknownHostException) {
                _error.value = "Network error: Please check your internet connection"
            } catch (e: SocketTimeoutException) {
                _error.value = "Connection timeout: Please try again"
            } catch (e: Exception) {
                _error.value = "Error loading data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshPlants() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val plantsResponse = api.getPlants()
                if (plantsResponse.isSuccessful) {
                    _plants.clear()
                    plantsResponse.body()?.let { plantResponses ->
                        try {
                            _plants.addAll(plantResponses.mapNotNull { it.toPlant() })
                        } catch (e: Exception) {
                            _error.value = "Error processing plant data: ${e.message}"
                        }
                    }
                } else {
                    _error.value = when (plantsResponse.code()) {
                        404 -> "No plants found"
                        401 -> "Unauthorized access"
                        403 -> "Access forbidden"
                        else -> "Failed to load plants: ${plantsResponse.code()}"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error refreshing plants: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshObservations() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val observationsResponse = api.getObservations()
                if (observationsResponse.isSuccessful) {
                    _observations.clear()
                    observationsResponse.body()?.let { observationResponses ->
                        try {
                            _observations.addAll(observationResponses.mapNotNull { it.toObservation() })
                        } catch (e: Exception) {
                            _error.value = "Error processing observation data: ${e.message}"
                        }
                    }
                } else {
                    _error.value = when (observationsResponse.code()) {
                        404 -> "No observations found"
                        401 -> "Unauthorized access"
                        403 -> "Access forbidden"
                        else -> "Failed to load observations: ${observationsResponse.code()}"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error refreshing observations: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun PlantResponse.toPlant(): Plant? {
        return try {
            Plant(
                id = id.toString(),
                commonName = commonName.ifBlank { "Unknown" },
                scientificName = scientificName.ifBlank { "Unknown" },
                habitat = habitat.ifBlank { "Unknown" },
                origin = origin,
                description = description,
                plantImageUrl = plantImage,
                createdByUserId = createdBy.toString()
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun ObservationResponse.toObservation(): Observation? {
        return try {
            Observation(
                id = id.toString(),
                relatedPlantId = relatedPlant?.id?.toString(),
                relatedPlantName = relatedPlant?.commonName?.ifBlank { "Unknown Plant" } ?: "Unknown Plant",
                observationImageUrl = observationImage,
                date = date.ifBlank { "Unknown Date" },
                time = time.ifBlank { "Unknown Time" },
                location = location.ifBlank { "Unknown Location" },
                note = note,
                createdByUserId = createdBy.toString()
            )
        } catch (e: Exception) {
            null
        }
    }
}
