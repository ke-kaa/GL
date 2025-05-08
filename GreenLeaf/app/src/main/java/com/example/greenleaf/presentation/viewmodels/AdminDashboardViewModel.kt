package com.example.greenleaf.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenleaf.data.remote.api.GreenLeafApi
import com.example.greenleaf.data.remote.models.UserStatsResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val api: GreenLeafApi
) : ViewModel() {

    private val _users = MutableStateFlow<List<UserStatsResponse>>(emptyList())
    val users: StateFlow<List<UserStatsResponse>> = _users.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = api.getAllUsers()
                if (response.isSuccessful) {
                    _users.value = response.body() ?: emptyList()
                } else {
                    _error.value = when (response.code()) {
                        401 -> "Unauthorized access"
                        403 -> "Access forbidden"
                        else -> "Failed to load users: ${response.code()}"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error loading users: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
} 