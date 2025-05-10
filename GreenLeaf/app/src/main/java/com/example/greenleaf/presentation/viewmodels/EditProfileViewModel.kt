package com.example.greenleaf.presentation.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenleaf.data.remote.api.GreenLeafApi
import com.example.greenleaf.data.remote.models.UpdateProfileRequest
import com.example.greenleaf.data.remote.models.UserProfileResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val api: GreenLeafApi
) : ViewModel() {

    private val _user = MutableStateFlow<UserProfileResponse?>(null)
    val user: StateFlow<UserProfileResponse?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = api.getUserProfile()
                if (response.isSuccessful) {
                    _user.value = response.body()
                } else {
                    _error.value = when (response.code()) {
                        401 -> "Unauthorized access"
                        403 -> "Access forbidden"
                        else -> "Failed to load profile: ${response.code()}"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error loading profile: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateFirstName(firstName: String) {
        _user.value = _user.value?.copy(firstName = firstName)
    }

    fun updateLastName(lastName: String) {
        _user.value = _user.value?.copy(lastName = lastName)
    }

    fun updateEmail(email: String) {
        _user.value = _user.value?.copy(email = email)
    }

    fun updateBirthdate(birthdate: String) {
        _user.value = _user.value?.copy(birthdate = birthdate)
    }

    fun updateGender(gender: String) {
        _user.value = _user.value?.copy(gender = gender)
    }

    fun updatePhoneNumber(phoneNumber: String) {
        _user.value = _user.value?.copy(phoneNumber = phoneNumber)
    }

    fun saveProfile(context: Context, imageUri: Uri?) {
        val current = _user.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // helper to wrap text
                fun String.toBody() =
                    toRequestBody("text/plain".toMediaTypeOrNull())

                val firstNameBody = (current.firstName ?: "").toBody()
                val lastNameBody  = (current.lastName  ?: "").toBody()
                val birthBody     = current.birthdate?.toBody()
                val genderBody    = current.gender?.toBody()
                val phoneBody     = current.phoneNumber?.toBody()

                // build image part if user picked one
                val imagePart = imageUri?.let { uri ->
                    val stream = context.contentResolver.openInputStream(uri)!!
                    val bytes  = stream.readBytes()
                    val reqBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData(
                        name     = "profile_image",
                        filename = "profile.jpg",
                        body     = reqBody
                    )
                }

                // call the multipart endpoint
                val resp = api.updateUserProfileMultipart(
                    firstNameBody,
                    lastNameBody,
                    birthBody,
                    genderBody,
                    phoneBody,
                    imagePart
                )

                if (resp.isSuccessful) {
                    _isSaved.value = true
                } else {
                    _error.value = "Failed: ${resp.code()} ${resp.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

}
