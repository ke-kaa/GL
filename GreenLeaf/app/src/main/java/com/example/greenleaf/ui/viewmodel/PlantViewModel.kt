package com.example.greenleaf.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenleaf.data.repositories.PlantRepository
import com.example.greenleaf.data.local.entities.PlantEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlantViewModel @Inject constructor(
    private val repository: PlantRepository
) : ViewModel() {
    private val _plants = MutableStateFlow<List<PlantEntity>>(emptyList())
    val plants: StateFlow<List<PlantEntity>> = _plants

    fun loadPlants() {
        viewModelScope.launch {
            _plants.value = repository.getPlants()
        }
    }

    fun addPlant(plant: PlantEntity) {
        viewModelScope.launch {
            repository.addPlant(plant)
            loadPlants()
        }
    }

    fun updatePlant(plant: PlantEntity) {
        viewModelScope.launch {
            repository.updatePlant(plant)
            loadPlants()
        }
    }

    fun deletePlant(plant: PlantEntity) {
        viewModelScope.launch {
            repository.deletePlant(plant)
            loadPlants()
        }
    }
}
