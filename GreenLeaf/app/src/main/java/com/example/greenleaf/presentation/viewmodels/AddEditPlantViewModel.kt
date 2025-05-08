package com.example.greenleaf.presentation.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenleaf.data.remote.api.GreenLeafApi
import com.example.greenleaf.data.remote.models.PlantRequest
import com.example.greenleaf.data.remote.models.PlantResponse
import com.example.greenleaf.fakedata.Plant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.net.UnknownHostException
import java.net.SocketTimeoutException

@HiltViewModel
class AddEditPlantViewModel @Inject constructor(
    private val api: GreenLeafApi
) : ViewModel() {
    // Form state
    val commonName = mutableStateOf("")
    val scientificName = mutableStateOf("")
    val habitat = mutableStateOf("")
    val origin = mutableStateOf("")
    val description = mutableStateOf("")
    val isSaved = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)
    val isLoading = mutableStateOf(false)
    val plantId = mutableStateOf<String?>(null)

    private var editingPlantId: String? = null

    /** Load existing plant data for editing */
    fun loadPlant(id: String) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val response = api.getPlantDetail(id.toInt())
                if (response.isSuccessful) {
                    response.body()?.let { plantResponse ->
                        editingPlantId = plantResponse.id.toString()
                        plantId.value = plantResponse.id.toString()
                        commonName.value = plantResponse.commonName
                        scientificName.value = plantResponse.scientificName
                        habitat.value = plantResponse.habitat
                        origin.value = plantResponse.origin ?: ""
                        description.value = plantResponse.description ?: ""
                    }
                } else {
                    error.value = when (response.code()) {
                        404 -> "Plant not found"
                        401 -> "Unauthorized access"
                        403 -> "Access forbidden"
                        else -> "Failed to load plant: ${response.code()}"
                    }
                }
            } catch (e: UnknownHostException) {
                error.value = "Network error: Please check your internet connection"
            } catch (e: SocketTimeoutException) {
                error.value = "Connection timeout: Please try again"
            } catch (e: Exception) {
                error.value = "Error loading plant: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun savePlant(onSuccess: (String) -> Unit) {
        if (commonName.value.isBlank() || scientificName.value.isBlank() || habitat.value.isBlank()) {
            error.value = "Please fill in all required fields"
            return
        }

        viewModelScope.launch {
            isLoading.value = true
            try {
                val plantRequest = PlantRequest(
                    commonName = commonName.value,
                    scientificName = scientificName.value,
                    habitat = habitat.value,
                    origin = origin.value.takeIf { it.isNotBlank() },
                    description = description.value.takeIf { it.isNotBlank() },
                    plantImage = null // TODO: Handle image upload
                )

                val response = if (editingPlantId != null) {
                    api.updatePlant(editingPlantId!!.toInt(), plantRequest)
                } else {
                    api.createPlant(plantRequest)
                }

                if (response.isSuccessful) {
                    response.body()?.let { plantResponse ->
                        isSaved.value = true
                        plantId.value = plantResponse.id.toString()
                        onSuccess(plantResponse.id.toString())
                    }
                } else {
                    error.value = when (response.code()) {
                        401 -> "Unauthorized access"
                        403 -> "Access forbidden"
                        else -> "Failed to save plant: ${response.code()}"
                    }
                }
            } catch (e: UnknownHostException) {
                error.value = "Network error: Please check your internet connection"
            } catch (e: SocketTimeoutException) {
                error.value = "Connection timeout: Please try again"
            } catch (e: Exception) {
                error.value = "Error saving plant: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }
}
