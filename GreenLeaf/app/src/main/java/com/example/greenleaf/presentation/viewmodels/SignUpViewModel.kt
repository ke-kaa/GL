// presentation/viewmodel/SignUpViewModel.kt
package com.example.greenleaf.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenleaf.data.remote.api.GreenLeafApi
import com.example.greenleaf.data.remote.interceptors.AuthInterceptor
import com.example.greenleaf.data.remote.models.RegisterRequest
import com.example.greenleaf.domain.utils.Validator
import com.example.greenleaf.presentation.ui.signup.SignUpEvent
import com.example.greenleaf.presentation.ui.signup.SignUpUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val api: GreenLeafApi,
    private val authInterceptor: AuthInterceptor
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun onEvent(event: SignUpEvent) {
        when (event) {
            is SignUpEvent.EmailChanged -> _uiState.update { it.copy(email = event.value) }
            is SignUpEvent.PasswordChanged -> _uiState.update { it.copy(password = event.value) }
            is SignUpEvent.ConfirmPasswordChanged -> _uiState.update { it.copy(confirmPassword = event.value) }
            is SignUpEvent.Submit -> register()
        }
    }

    private fun register() {
        val currentState = _uiState.value

        // Validate inputs
        if (!Validator.isValidEmail(currentState.email)) {
            _uiState.update { it.copy(error = "Invalid email format") }
            return
        }

        if (!Validator.isValidPassword(currentState.password)) {
            _uiState.update { it.copy(error = "Password must be at least 6 characters") }
            return
        }

        if (currentState.password != currentState.confirmPassword) {
            _uiState.update { it.copy(error = "Passwords do not match") }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                val response = api.register(
                    RegisterRequest(
                        email = currentState.email,
                        password = currentState.password,
                        confirmPassword = currentState.confirmPassword
                    )
                )

                if (response.isSuccessful) {
                    response.body()?.let { registerResponse ->
                        // Store tokens for future requests
                        authInterceptor.setTokens(registerResponse.access, registerResponse.refresh)
                        _uiState.update { it.copy(isLoading = false, success = true) }
                    } ?: run {
                        _uiState.update { it.copy(isLoading = false, error = "Empty response from server") }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = "Registration failed: ${errorBody ?: response.message()}"
                        ) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Registration failed: ${e.message}") }
            }
        }
    }
}
