package com.epsilonlabs.uavpathcalculator.utils.tsp

import android.location.Location
import android.util.Log
import com.epsilonlabs.uavpathcalculator.database.entities.UavEntity
import com.google.android.gms.maps.model.Marker
import java.time.Duration
import java.time.LocalTime

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
     * Calculates distances in meters between 2 LatLng points
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
    fun createSchedule(
        uav: UavEntity,
        path: ArrayList<Marker>,
        departureTime: LocalTime,
        timeMonitoring: Duration,
        timeCharging: Duration,
        abrasSpeed: Int
    ): ArrayList<ResultData> {
        val result = arrayListOf<ResultData>()

        result.add(
            ResultData(
                path[0],
                false,
                departureTime,
                departureTime,
                timeMonitoring,
                Duration.ofMinutes(uav.flightTime!!.toLong())
            )
        )
        for(i in 1 until path.size) {
            //todo should be more precise, maybe change it to seconds
            val flightTime = Duration.ofMinutes(
                (calculateDistance(path[i], path[i-1]) / (uav.speed!! * 16.6667)).toLong()
            )
            val arrivalTime = result[i-1].arrivalTime!! + flightTime
            Log.e("TSP", "Point [$i] Flight time: $flightTime Arrival: $arrivalTime")
            result.add(
                ResultData(
                    path[i],
                    false,
                    arrivalTime,
                    null,
                    null,
                    null
                )
            )
        }
        return result
    }
}