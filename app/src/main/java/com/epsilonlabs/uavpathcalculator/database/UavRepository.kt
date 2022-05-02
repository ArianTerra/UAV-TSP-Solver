package com.epsilonlabs.uavpathcalculator.database

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class UavRepository(private val uavDao: UavDao) {
    val allUavs: Flow<List<Uav>> = uavDao.getUAVs()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(uav: Uav) {
        uavDao.insertUAV(uav)
    }
}