package com.example.greenleaf.presentation.viewmodels

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenleaf.data.remote.api.GreenLeafApi
import com.example.greenleaf.data.remote.models.ObservationRequest
import com.example.greenleaf.data.remote.models.ObservationResponse
import com.example.greenleaf.data.remote.models.PlantResponse
import com.example.greenleaf.fakedata.Observation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.net.UnknownHostException
import java.net.SocketTimeoutException

@HiltViewModel
class AddEditObservationViewModel @Inject constructor(
    private val api: GreenLeafApi
) : ViewModel() {
    var observation by mutableStateOf(
        Observation(
            id = "",
            relatedPlantId = null,
            relatedPlantName = "",
            observationImageUrl = null,
            date = "",
            time = "",
            location = "",
            note = "",
            createdByUserId = ""
        )
    )
        private set

    val error = mutableStateOf<String?>(null)
    val isLoading = mutableStateOf(false)
    val isSaved = mutableStateOf(false)
    val plants = mutableStateOf<List<PlantResponse>>(emptyList())
    var selectedPlant by mutableStateOf<PlantResponse?>(null)
        private set

    init {
        loadPlants()
    }

    private fun loadPlants() {
        viewModelScope.launch {
            try {
                val response = api.getPlants()
                if (response.isSuccessful) {
                    plants.value = response.body() ?: emptyList()
                } else {
                    error.value = "Failed to load plants: ${response.code()}"
                }
            } catch (e: Exception) {
                error.value = "Error loading plants: ${e.message}"
            }
        }
    }

    fun onPlantSelected(plant: PlantResponse) {
        selectedPlant = plant
        observation = observation.copy(
            relatedPlantId = plant.id.toString(),
            relatedPlantName = plant.commonName
        )
    }

    fun loadObservation(id: String) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val response = api.getObservationDetail(id.toInt())
                if (response.isSuccessful) {
                    response.body()?.let { obsResponse ->
                        observation = Observation(
                            id = obsResponse.id.toString(),
                            relatedPlantId = obsResponse.relatedPlant?.id?.toString(),
                            relatedPlantName = obsResponse.relatedPlant?.commonName ?: "Unknown Plant",
                            observationImageUrl = obsResponse.observationImage,
                            date = obsResponse.date,
                            time = obsResponse.time,
                            location = obsResponse.location,
                            note = obsResponse.note,
                            createdByUserId = obsResponse.createdBy.toString()
                        )
                        // Set the selected plant if there is a related plant
                        obsResponse.relatedPlant?.let { plant ->
                            selectedPlant = plant
                        }
                    }
                } else {
                    error.value = when (response.code()) {
                        404 -> "Observation not found"
                        401 -> "Unauthorized access"
                        403 -> "Access forbidden"
                        else -> "Failed to load observation: ${response.code()}"
                    }
                }
            } catch (e: UnknownHostException) {
                error.value = "Network error: Please check your internet connection"
            } catch (e: SocketTimeoutException) {
                error.value = "Connection timeout: Please try again"
            } catch (e: Exception) {
                error.value = "Error loading observation: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun onDateChange(value: String) {
        observation = observation.copy(date = value)
    }

    fun onTimeChange(value: String) {
        observation = observation.copy(time = value)
    }

    fun onLocationChange(value: String) {
        observation = observation.copy(location = value)
    }

    fun onNotesChange(value: String) {
        observation = observation.copy(note = value)
    }

    fun saveObservation(onSuccess: (String) -> Unit) {
        if (selectedPlant == null || observation.date.isBlank() || 
            observation.time.isBlank() || observation.location.isBlank()) {
            error.value = "Please fill in all required fields"
            return
        }

        viewModelScope.launch {
            isLoading.value = true
            try {
                val observationRequest = ObservationRequest(
                    relatedPlantId = selectedPlant?.id,
                    observationImage = observation.observationImageUrl,
                    time = observation.time,
                    date = observation.date,
                    location = observation.location,
                    note = observation.note
                )

                val response = if (observation.id.isNotBlank()) {
                    api.updateObservation(observation.id.toInt(), observationRequest)
                } else {
                    api.createObservation(observationRequest)
                }

                if (response.isSuccessful) {
                    response.body()?.let { obsResponse ->
                        // Update the observation with the new data
                        observation = observation.copy(
                            id = obsResponse.id.toString(),
                            relatedPlantId = obsResponse.relatedPlant?.id?.toString(),
                            relatedPlantName = obsResponse.relatedPlant?.commonName ?: selectedPlant?.commonName ?: "Unknown Plant",
                            observationImageUrl = obsResponse.observationImage,
                            date = obsResponse.date,
                            time = obsResponse.time,
                            location = obsResponse.location,
                            note = obsResponse.note,
                            createdByUserId = obsResponse.createdBy.toString()
                        )
                        isSaved.value = true
                        onSuccess(obsResponse.id.toString())
                    }
                } else {
                    error.value = when (response.code()) {
                        401 -> "Unauthorized access"
                        403 -> "Access forbidden"
                        else -> "Failed to save observation: ${response.code()}"
                    }
                }
            } catch (e: UnknownHostException) {
                error.value = "Network error: Please check your internet connection"
            } catch (e: SocketTimeoutException) {
                error.value = "Connection timeout: Please try again"
            } catch (e: Exception) {
                error.value = "Error saving observation: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }
}
