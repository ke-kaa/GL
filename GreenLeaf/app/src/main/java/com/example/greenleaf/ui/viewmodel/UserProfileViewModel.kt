package com.example.greenleaf.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenleaf.data.repositories.UserProfileRepository
import com.example.greenleaf.data.local.entities.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val repository: UserProfileRepository
) : ViewModel() {
    private val _profile = MutableStateFlow<UserEntity?>(null)
    val profile: StateFlow<UserEntity?> = _profile

    fun loadProfile() {
        viewModelScope.launch {
            _profile.value = repository.getProfile()
        }
    }

    fun updateProfile(profile: UserEntity) {
        viewModelScope.launch {
            repository.updateProfile(profile)
            loadProfile()
        }
    }
}
