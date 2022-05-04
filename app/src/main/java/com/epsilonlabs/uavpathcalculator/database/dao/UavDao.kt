package com.epsilonlabs.uavpathcalculator.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.epsilonlabs.uavpathcalculator.database.entities.UavEntity
import kotlinx.coroutines.flow.Flow

/**
 * Dao for Uav class using ROOM db.
 * @author Artem Serediuk
 */
@Dao
interface UavDao {
    @Query("SELECT * FROM uav_table")
    fun getAll() : Flow<List<UavEntity>>

    @Query("SELECT * FROM uav_table WHERE id = :id")
    fun getById(id: Int): UavEntity

    @Insert
    fun insert(uav: UavEntity) //not used
    
    @Query("DELETE FROM uav_table")
    suspend fun deleteAll()
}