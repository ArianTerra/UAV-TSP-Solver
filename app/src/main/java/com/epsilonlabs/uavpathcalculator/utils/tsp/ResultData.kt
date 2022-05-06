package com.epsilonlabs.uavpathcalculator.utils.tsp

import com.google.android.gms.maps.model.Marker
import java.time.Duration
import java.time.LocalTime

data class ResultData(
    val marker: Marker,
    val isPBR: Boolean,
    val arrivalTime: LocalTime?,
    val departureTime: LocalTime?,
    val timeSpent: Duration?,
    val batteryTimeLeft: Duration?
    )