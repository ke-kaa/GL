package com.example.greenleaf.presentation.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenleaf.data.remote.api.GreenLeafApi
import com.example.greenleaf.data.remote.models.ObservationResponse
import com.example.greenleaf.fakedata.Observation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.net.UnknownHostException
import java.net.SocketTimeoutException

@HiltViewModel
class ObservationDetailViewModel @Inject constructor(
    private val api: GreenLeafApi
) : ViewModel() {
    var observation by mutableStateOf<Observation?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    /** Load a single observation for details */
    fun loadObservation(id: String) {
        if (id.isBlank()) {
            error = "Invalid observation ID"
            return
        }

        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                val observationId = id.toIntOrNull()
                if (observationId == null) {
                    error = "Invalid observation ID format"
                    return@launch
                }

                val response = api.getObservationDetail(observationId)
                if (response.isSuccessful) {
                    response.body()?.let { observationResponse ->
                        try {
                            observation = observationResponse.toObservation()
                        } catch (e: Exception) {
                            error = "Error processing observation data: ${e.message}"
                        }
                    } ?: run {
                        error = "Observation not found"
                    }
                } else {
                    error = when (response.code()) {
                        404 -> "Observation not found"
                        401 -> "Unauthorized access"
                        403 -> "Access forbidden"
                        else -> "Failed to load observation: ${response.code()}"
                    }
                }
            } catch (e: UnknownHostException) {
                error = "Network error: Please check your internet connection"
            } catch (e: SocketTimeoutException) {
                error = "Connection timeout: Please try again"
            } catch (e: Exception) {
                error = "Error loading observation: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    /** Delete and clear */
    fun deleteObservation(id: String) {
        if (id.isBlank()) {
            error = "Invalid observation ID"
            return
        }

        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                val observationId = id.toIntOrNull()
                if (observationId == null) {
                    error = "Invalid observation ID format"
                    return@launch
                }

                val response = api.deleteObservation(observationId)
                if (response.isSuccessful) {
                    observation = null
                } else {
                    error = when (response.code()) {
                        404 -> "Observation not found"
                        401 -> "Unauthorized access"
                        403 -> "Access forbidden"
                        else -> "Failed to delete observation: ${response.code()}"
                    }
                }
            } catch (e: UnknownHostException) {
                error = "Network error: Please check your internet connection"
            } catch (e: SocketTimeoutException) {
                error = "Connection timeout: Please try again"
            } catch (e: Exception) {
                error = "Error deleting observation: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    private fun ObservationResponse.toObservation(): Observation {
        return Observation(
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
    }
}
