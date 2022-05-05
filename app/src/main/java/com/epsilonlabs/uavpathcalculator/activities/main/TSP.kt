package com.epsilonlabs.uavpathcalculator.activities.main

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

/**
 * Solves Traveling Salesman Problem by creating a distance matrix
 * and using different algorithms (only one available now, TODO).
 * First element in ArrayList<Marker> is the starting point.
 * @author Artem Serediuk
 */
class TSP(private val markers : ArrayList<Marker>) {
    private val distanceMatrix : Array<FloatArray> = Array(markers.size) {
        FloatArray(markers.size)
    }

    init {
        //create distance matrix
        for(i in 0 until markers.size) {
            for (j in 0 until markers.size) {
                var distance = calculateDistance(markers[i], markers[j])
                if(distance == 0.0f) distance = Float.POSITIVE_INFINITY
                distanceMatrix[i][j] = distance
            }
        }
    }

    /**
     * Calculates distances between 2 LatLng points
     */
    private fun calculateDistance(markerA : Marker, markerB : Marker) : Float {
        val result = FloatArray(1)
        Location.distanceBetween(
            markerA.position.latitude,
            markerA.position.longitude,
            markerB.position.latitude,
            markerB.position.longitude,
            result
        )
        return result[0]
    }

    /**
     * Solves TSP problem using nearest neighbor algorithm and returns path
     */
    fun calculateNearestNeighbor() : ArrayList<Marker> {
        val path = arrayListOf<Marker>()
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