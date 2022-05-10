package com.epsilonlabs.uavpathcalculator.utils

import android.location.Location

class MapUtils {
    companion object {
        /**
         * Calculates distances in meters between 2 LatLng points
         */
        fun calculateDistance(markerA : MarkerParcelable, markerB : MarkerParcelable) : Float {
            val result = FloatArray(1)
            Location.distanceBetween(
                markerA.latitude,
                markerA.longitude,
                markerB.latitude,
                markerB.longitude,
                result
            )
            return result[0]
        }
    }
}