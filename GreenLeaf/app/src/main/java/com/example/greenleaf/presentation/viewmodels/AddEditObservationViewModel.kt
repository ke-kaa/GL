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
import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

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
    /**
     * Call this before saveObservation() to convert the picked Uri into
     * a Base64 string and store it in the observationImageUrl field.
     */
    /** Convert picked Uri → Base64 and store it */
    fun onImageSelected(uri: Uri?, ctx: Context) {
        uri?.let {
            val bytes = ctx.contentResolver.openInputStream(it)?.readBytes()
            val base64 = bytes?.let { arr -> android.util.Base64.encodeToString(arr, android.util.Base64.NO_WRAP) }
            observation = observation.copy(observationImageUrl = base64)
        }
    }


    fun saveObservation(
        context: Context,
        imageUri: Uri?,
        onSuccess: (String) -> Unit
    ) {
        if (selectedPlant == null ||
            observation.date.isBlank() ||
            observation.time.isBlank() ||
            observation.location.isBlank()) {
            error.value = "Please fill in all required fields"
            return
        }

        viewModelScope.launch {
            isLoading.value = true
            error.value = null
            try {
                // 1) wrap text fields
                fun String.toPart() = toRequestBody("text/plain".toMediaTypeOrNull())
                val plantIdPart  = selectedPlant!!.id.toString().toPart()
                val datePart     = observation.date.toPart()
                val timePart     = observation.time.toPart()
                val locationPart = observation.location.toPart()
                val notePart     = observation.note?.toPart()

                // 2) build image part from Uri
                val imagePart: MultipartBody.Part? = imageUri?.let { uri ->
                    val stream = context.contentResolver.openInputStream(uri)!!
                    val bytes  = stream.readBytes()
                    val reqFile = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData(
                        name = "observation_image",
                        filename = "photo.jpg",
                        body = reqFile
                    )
                }

                // 3) call multipart endpoint
                val response = if (observation.id.isNotBlank()) {
                    api.updateObservationMultipart(
                        observation.id.toInt(),
                        plantIdPart, datePart, timePart, locationPart, notePart, imagePart
                    )
                } else {
                    api.createObservationMultipart(
                        plantIdPart, datePart, timePart, locationPart, notePart, imagePart
                    )
                }

                // 4) handle response
                if (response.isSuccessful) {
                    response.body()?.let { resp ->
                        observation = observation.copy(
                            id = resp.id.toString(),
                            observationImageUrl = resp.observationImage,
                            date = resp.date,
                            time = resp.time,
                            location = resp.location,
                            note = resp.note,
                            relatedPlantName = resp.relatedPlant?.commonName ?: observation.relatedPlantName
                        )
                        isSaved.value = true
                        onSuccess(resp.id.toString())
                    }
                } else {
                    error.value = "Save failed: ${response.code()}"
                }
            } catch (e: Exception) {
                error.value = "Error: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

}
