package com.example.greenleaf.presentation.viewmodels

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

    fun saveProfile() {
        val currentUser = _user.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val request = UpdateProfileRequest(
                    firstName = currentUser.firstName,
                    lastName = currentUser.lastName,
                    birthdate = currentUser.birthdate,
                    gender = currentUser.gender,
                    phoneNumber = currentUser.phoneNumber,
                    profileImage = currentUser.profileImage
                )
                val response = api.updateUserProfile(request)
                if (response.isSuccessful) {
                    _isSaved.value = true
                } else {
                    _error.value = when (response.code()) {
                        401 -> "Unauthorized access"
                        403 -> "Access forbidden"
                        else -> "Failed to update profile: ${response.code()}"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error updating profile: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
