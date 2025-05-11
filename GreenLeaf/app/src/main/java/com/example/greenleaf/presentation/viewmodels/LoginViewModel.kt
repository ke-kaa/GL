// presentation/viewmodel/LoginViewModel.kt
package com.example.greenleaf.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenleaf.data.remote.api.GreenLeafApi
import com.example.greenleaf.data.remote.interceptors.AuthInterceptor
import com.example.greenleaf.data.remote.models.LoginRequest
import com.example.greenleaf.data.remote.models.LoginResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.navigation.NavController

//
//@HiltViewModel
//class LoginViewModel @Inject constructor(
//    private val api: GreenLeafApi,
//    private val authInterceptor: AuthInterceptor
//) : ViewModel() {
//
////    private val _uiState = MutableStateFlow(LoginUiState())
////    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
//
//    private val _uiState = MutableStateFlow(LoginUiState())
//    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
//
//    fun onEmailChanged(email: String) {
//        _uiState.update { it.copy(email = email) }
//    }
//
//    fun onPasswordChanged(password: String) {
//        _uiState.update { it.copy(password = password) }
//    }
//
//    fun onLoginClicked() {
//        viewModelScope.launch {
//            try {
//                _uiState.update { it.copy(isLoading = true, error = null) }
//
//                val response = api.login(LoginRequest(_uiState.value.email, _uiState.value.password))
//
//                if (response.isSuccessful) {
//                    response.body()?.let { loginResponse ->
//                        // Store token for future requests
//                        authInterceptor.setToken(loginResponse.access)
//                        _uiState.update { it.copy(isLoading = false, success = true) }
//                    } ?: run {
//                        _uiState.update { it.copy(isLoading = false, error = "Empty response from server") }
//                    }
//                } else {
//                    _uiState.update { it.copy(isLoading = false, error = "Login failed: ${response.message()}") }
//                }
//            } catch (e: Exception) {
//                _uiState.update { it.copy(isLoading = false, error = "Login failed: ${e.message}") }
//            }
//        }
//    }
//}
//
//data class LoginUiState(
//    val email: String = "",
//    val password: String = "",
//    val isLoading: Boolean = false,
//    val error: String? = null,
//    val success: Boolean = false
//)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val api: GreenLeafApi,
    private val authInterceptor: AuthInterceptor
) : ViewModel() {

//    private val _uiState = MutableStateFlow(LoginUiState())
//    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = api.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    response.body()?.let { loginResponse ->
                        // Store tokens for future requests
                        authInterceptor.setTokens(loginResponse.access, loginResponse.refresh)
                        
                        // Check if user is admin by getting all users and finding the current user
                        val usersResponse = api.getAllUsers()
                        if (usersResponse.isSuccessful) {
                            val users = usersResponse.body()
                            val currentUser = users?.find { it.email == email }
                            _uiState.update { 
                                it.copy(
                                    isLoading = false, 
                                    success = true,
                                    isAdmin = currentUser?.isAdmin ?: false
                                ) 
                            }
                        } else {
                            _uiState.update { it.copy(isLoading = false, success = true) }
                        }
                    } ?: run {
                        _uiState.update { it.copy(isLoading = false, error = "Empty response from server") }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = "Login failed: ${errorBody ?: response.message()}"
                        ) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Login failed: ${e.message}") }
            }
        }
    }
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val isAdmin: Boolean = false
)


