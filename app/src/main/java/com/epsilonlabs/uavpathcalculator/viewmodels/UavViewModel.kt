package com.epsilonlabs.uavpathcalculator.viewmodels

import androidx.lifecycle.*
import com.epsilonlabs.uavpathcalculator.database.AppRepository
import com.epsilonlabs.uavpathcalculator.database.entities.UavEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * View model for live data changing
 */
class UavViewModel(private val repository: AppRepository) : ViewModel() {
    val allUavs: LiveData<List<UavEntity>> = repository.allUavs.asLiveData()

    //TODO change to CoroutineScope
    fun insert(uav: UavEntity) = viewModelScope.launch {
        repository.insert(uav)
    }

    fun getById(id: Int) : LiveData<UavEntity>  {
        val result = MutableLiveData<UavEntity>()
        // seems like viewModelScope.launch is using main thread
        // so I decided to use this instead
        CoroutineScope(Dispatchers.IO).launch {
            val returned = repository.getById(id)
            result.postValue(returned)
        }
        return result
    }
}

class UavViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory{
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(UavViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UavViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}