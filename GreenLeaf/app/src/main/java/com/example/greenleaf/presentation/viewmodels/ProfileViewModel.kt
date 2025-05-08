package com.example.greenleaf.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenleaf.data.remote.api.GreenLeafApi
import com.example.greenleaf.data.remote.interceptors.AuthInterceptor
import com.example.greenleaf.data.remote.models.LogoutRequest
import com.example.greenleaf.data.remote.models.UserProfileResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val api: GreenLeafApi,
    private val authInterceptor: AuthInterceptor
) : ViewModel() {
    private val _isLoggedOut = MutableStateFlow(false)
    val isLoggedOut: StateFlow<Boolean> = _isLoggedOut.asStateFlow()

    private val _showDeleteDlg = MutableStateFlow(false)
    val showDeleteDlg: StateFlow<Boolean> = _showDeleteDlg.asStateFlow()

    private val _user = MutableStateFlow<UserProfileResponse?>(null)
    val user: StateFlow<UserProfileResponse?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

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

    fun logout() {
        viewModelScope.launch {
            try {
                val refreshToken = authInterceptor.getRefreshToken()
                if (refreshToken == null) {
                    _error.value = "No refresh token found"
                    return@launch
                }
                val response = api.logout(LogoutRequest(refresh = refreshToken))
                if (response.isSuccessful) {
                    authInterceptor.setTokens(null, null) // Clear tokens
                    _isLoggedOut.value = true
                } else {
                    _error.value = when (response.code()) {
                        400 -> "Invalid refresh token"
                        401 -> "Unauthorized access"
                        403 -> "Access forbidden"
                        else -> "Failed to logout: ${response.code()}"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error during logout: ${e.message}"
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            try {
                val response = api.deleteAccount()
                if (response.isSuccessful) {
                    authInterceptor.setTokens(null, null) // Clear tokens
                    _isLoggedOut.value = true
                } else {
                    _error.value = "Failed to delete account: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error deleting account: ${e.message}"
            }
        }
    }

    fun showDeleteDialog(show: Boolean) {
        _showDeleteDlg.value = show
    }
}
