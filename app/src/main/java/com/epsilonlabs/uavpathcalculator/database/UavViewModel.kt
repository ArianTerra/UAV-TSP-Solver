package com.epsilonlabs.uavpathcalculator.database

import androidx.lifecycle.*
import kotlinx.coroutines.launch

class UavViewModel(private val repository: UavRepository) : ViewModel() {
    val allUavs: LiveData<List<Uav>> = repository.allUavs.asLiveData()

    fun insert(uav: Uav) = viewModelScope.launch {
        repository.insert(uav)
    }
}

class UavViewModelFactory(private val repository: UavRepository) : ViewModelProvider.Factory{
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(UavViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UavViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}