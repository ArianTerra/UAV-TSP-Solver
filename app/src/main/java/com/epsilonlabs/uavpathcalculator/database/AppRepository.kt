package com.epsilonlabs.uavpathcalculator.database

import androidx.annotation.WorkerThread
import com.epsilonlabs.uavpathcalculator.database.dao.UavDao
import com.epsilonlabs.uavpathcalculator.database.entities.UavEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository for ROOM db
 */
class AppRepository(private val uavDao: UavDao) {
    val allUavs: Flow<List<UavEntity>> = uavDao.getAll()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(uav: UavEntity) {
        uavDao.insert(uav)
    }
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getById(id: Int) : UavEntity {
        return uavDao.getById(id)
    }
}