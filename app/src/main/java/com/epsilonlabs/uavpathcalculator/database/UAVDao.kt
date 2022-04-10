package com.epsilonlabs.uavpathcalculator.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UAVDao {
    @Query("SELECT * FROM UAV")
    fun getUAV() : List<UAV>
    @Insert
    fun insertUAV(uav: List<UAV>)
}