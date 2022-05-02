package com.epsilonlabs.uavpathcalculator.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UavDao {
    @Query("SELECT * FROM Uav")
    fun getUAVs() : Flow<List<Uav>> //flow is for data binding
    @Insert
    suspend fun insertUAV(uav: Uav)
}