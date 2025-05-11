package com.example.greenleaf.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenleaf.data.repositories.ObservationRepository
import com.example.greenleaf.data.local.entities.ObservationEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ObservationViewModel @Inject constructor(
    private val repository: ObservationRepository
) : ViewModel() {
    private val _observations = MutableStateFlow<List<ObservationEntity>>(emptyList())
    val observations: StateFlow<List<ObservationEntity>> = _observations

    fun loadObservations() {
        viewModelScope.launch {
            _observations.value = repository.getObservations()
        }
    }

    fun addObservation(observation: ObservationEntity) {
        viewModelScope.launch {
            repository.addObservation(observation)
            loadObservations()
        }
    }

    fun updateObservation(observation: ObservationEntity) {
        viewModelScope.launch {
            repository.updateObservation(observation)
            loadObservations()
        }
    }

    fun deleteObservation(observation: ObservationEntity) {
        viewModelScope.launch {
            repository.deleteObservation(observation)
            loadObservations()
        }
    }
}
