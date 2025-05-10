package com.example.greenleaf.presentation.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody


import com.example.greenleaf.data.remote.api.GreenLeafApi
import com.example.greenleaf.data.remote.models.PlantRequest
import com.example.greenleaf.data.remote.models.PlantResponse
import com.example.greenleaf.fakedata.Plant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.net.UnknownHostException
import java.net.SocketTimeoutException
import android.util.Base64
import android.content.Context
import android.net.Uri
import java.io.InputStream
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

@HiltViewModel
class AddEditPlantViewModel @Inject constructor(
    private val api: GreenLeafApi
) : ViewModel() {
    // Form state dameabera11@gmail.com  password

    val commonName = mutableStateOf("")
    val scientificName = mutableStateOf("")
    val habitat = mutableStateOf("")
    val origin = mutableStateOf("")
    val description = mutableStateOf("")
    val isSaved = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)
    val isLoading = mutableStateOf(false)
    val plantId = mutableStateOf<String?>(null)
//    val plantImageUri = mutableStateOf<Uri?>(null)

    private var editingPlantId: String? = null
    fun uriToBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            bytes?.let { Base64.encodeToString(it, Base64.NO_WRAP) }
        } catch (e: Exception) {
            null
        }
    }
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


    fun savePlant(context: Context, imageUri: Uri?, onSuccess: (String) -> Unit) {
        if (commonName.value.isBlank() || scientificName.value.isBlank() || habitat.value.isBlank()) {
            error.value = "Please fill in all required fields"
            return
        }

        viewModelScope.launch {
            isLoading.value = true
            error.value = null
            try {
                // --- wrap text fields as RequestBody ---
                fun String.toTextPart() =
                    toRequestBody("text/plain".toMediaTypeOrNull())

                val commonNamePart     = commonName.value.toTextPart()
                val scientificNamePart = scientificName.value.toTextPart()
                val habitatPart        = habitat.value.toTextPart()
                val originPart         = origin.value.takeIf(String::isNotBlank)?.toTextPart()
                val descriptionPart    = description.value.takeIf(String::isNotBlank)?.toTextPart()

                // --- build image file part ---
                val imagePart: MultipartBody.Part? = imageUri?.let { uri ->
                    val stream = context.contentResolver.openInputStream(uri)!!
                    val bytes  = stream.readBytes()
                    val reqFile = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData(
                        name = "plant_image",
                        filename = "upload.jpg",
                        body = reqFile
                    )
                }

                // --- call the correct multipart endpoint ---
                val response = if (editingPlantId != null) {
                    api.updatePlantMultipart(
                        editingPlantId!!.toInt(),
                        commonNamePart, scientificNamePart, habitatPart,
                        originPart, descriptionPart, imagePart
                    )
                } else {
                    api.createPlantMultipart(
                        commonNamePart, scientificNamePart, habitatPart,
                        originPart, descriptionPart, imagePart
                    )
                }

                // --- handle response ---
                if (response.isSuccessful) {
                    response.body()?.let { plantResponse ->
                        plantId.value = plantResponse.id.toString()
                        isSaved.value = true
                        onSuccess(plantResponse.id.toString())
                    }
                } else {
                    error.value = "Failed to save plant: ${response.code()}"
                }
            } catch (e: Exception) {
                error.value = "Error saving plant: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

}
