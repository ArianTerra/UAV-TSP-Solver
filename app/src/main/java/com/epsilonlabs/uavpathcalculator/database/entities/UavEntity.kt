package com.epsilonlabs.uavpathcalculator.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Basic UAV entity class for ROOM db
 * @author Artem Serediuk
 */
@Entity(tableName = "uav_table")
data class UavEntity(
    @PrimaryKey
    val id : Int,
    @ColumnInfo(name = "name")
    val name : String?,
    @ColumnInfo(name = "speed")
    val speed : Double?,
    @ColumnInfo(name = "flight_time")
    val flightTime : Int?
)
