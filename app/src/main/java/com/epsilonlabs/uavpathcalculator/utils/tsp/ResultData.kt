package com.epsilonlabs.uavpathcalculator.utils.tsp

import com.epsilonlabs.uavpathcalculator.database.entities.UavEntity
import com.epsilonlabs.uavpathcalculator.utils.MarkerParcelable
import com.google.android.gms.maps.model.Marker
import java.time.Duration
import java.time.LocalTime

data class ResultData(
    val marker: MarkerParcelable,
    val isPBR: Boolean,
    val uavName: String,
    val arrivalTime: LocalTime?,
    val departureTime: LocalTime?,
    val timeSpent: Duration?,
    val batteryTimeLeft: Duration?
    ) {
    override fun toString(): String {
        return "[${marker.title}] PBR: $isPBR UAV: $uavName A: $arrivalTime D: $departureTime " +
                "Spent: $timeSpent Battery: $batteryTimeLeft"
    }
}