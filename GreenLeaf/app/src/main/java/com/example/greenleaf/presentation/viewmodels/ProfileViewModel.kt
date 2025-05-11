package com.example.greenleaf.presentation.viewmodels
//dameabera11@gmail.com   password
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

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Get user profile
                val profileResponse = api.getUserProfile()
                if (profileResponse.isSuccessful) {
                    profileResponse.body()?.let { user ->
                        _user.value = user
                        
                        // Get admin status from users list
                        val usersResponse = api.getAllUsers()
                        if (usersResponse.isSuccessful) {
                            val users = usersResponse.body()
                            val currentUser = users?.find { it.email == user.email }
                            _isAdmin.value = currentUser?.isAdmin ?: false
                        }
                    }
                } else {
                    _error.value = "Failed to load profile"
                }
            } catch (e: Exception) {
                _error.value = "Error loading profile: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun showDeleteDialog(show: Boolean) {
        _showDeleteDlg.value = show
    }

    fun logout() {
        viewModelScope.launch {
            try {
                val response = api.logout(LogoutRequest(authInterceptor.getRefreshToken() ?: ""))
                if (response.isSuccessful) {
                    _isLoggedOut.value = true
                } else {
                    _error.value = "Failed to logout"
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
                    _isLoggedOut.value = true
                } else {
                    _error.value = "Failed to delete account"
                }
            } catch (e: Exception) {
                _error.value = "Error deleting account: ${e.message}"
            }
        }
    }
}
