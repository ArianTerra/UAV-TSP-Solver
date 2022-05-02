package com.epsilonlabs.uavpathcalculator.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Uav(
    @PrimaryKey
    val id : Int,
    @ColumnInfo(name = "name")
    val name : String?,
    @ColumnInfo(name = "speed")
    val speed : Double?,
    @ColumnInfo(name = "flight_time")
    val flightTime : Int?
)
