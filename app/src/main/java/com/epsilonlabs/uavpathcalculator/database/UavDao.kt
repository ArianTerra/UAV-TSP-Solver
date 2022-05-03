package com.epsilonlabs.uavpathcalculator.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Dao for Uav class using ROOM db.
 * @author Artem Serediuk
 */
@Dao
interface UavDao {
    @Query("SELECT * FROM uav_table")
    fun getAll() : Flow<List<Uav>>

    @Query("SELECT * FROM uav_table WHERE id = :id")
    fun getById(id: Int): Uav

    @Insert
    suspend fun insert(uav: Uav) //not used
    
    @Query("DELETE FROM uav_table")
    suspend fun deleteAll()
}