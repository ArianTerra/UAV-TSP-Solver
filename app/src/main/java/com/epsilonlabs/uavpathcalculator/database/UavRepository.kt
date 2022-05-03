package com.epsilonlabs.uavpathcalculator.database

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

/**
 * Repository for ROOM db
 */
class UavRepository(private val uavDao: UavDao) {
    val allUavs: Flow<List<Uav>> = uavDao.getAll()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(uav: Uav) {
        uavDao.insert(uav)
    }
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getById(id: Int) : Uav {
        return uavDao.getById(id)
    }
}