package com.epsilonlabs.uavpathcalculator.main

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import kotlin.collections.ArrayList

class TSP(markers : ArrayList<LatLng>) {
    //val distances: MutableMap<LatLng, Map<LatLng, Float>> = mutableMapOf()
    private val distanceMatrix : Array<FloatArray>
    private val markers : ArrayList<LatLng>

    init {
        this.markers = markers
        distanceMatrix = Array(markers.size) {FloatArray(markers.size)}
        for(i in 0 until markers.size) {
            for (j in 0 until markers.size) {
                var distance = calculateDistance(markers[i], markers[j])
                if(distance == 0.0f) distance = Float.POSITIVE_INFINITY
                distanceMatrix[i][j] = distance
            }
        }
    }

    private fun calculateDistance(markerA : LatLng, markerB : LatLng) : Float {
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

    fun calculateNearestNeighbor() : ArrayList<LatLng> {
        val path = arrayListOf<LatLng>()

        val pathIndexes = arrayListOf<Int>()

        pathIndexes.add(0)

        for (i in 1 until markers.size) {

            val s = arrayListOf<Float>()
            for (j in 0 until markers.size) {
                s.add(distanceMatrix[pathIndexes[i-1]][j])
            }
            pathIndexes.add(s.indexOf(s.minOrNull()))

            for(j in 0 until i) {
                distanceMatrix[pathIndexes[i]][pathIndexes[j]] = Float.POSITIVE_INFINITY
            }
        }

        for(i in pathIndexes) {
            path.add(markers[i])
        }
        return path
    }
}