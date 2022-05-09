package com.epsilonlabs.uavpathcalculator.utils.tsp

import android.location.Location
import android.util.Log
import com.epsilonlabs.uavpathcalculator.database.entities.UavEntity
import com.epsilonlabs.uavpathcalculator.utils.MarkerParcelable
import com.google.android.gms.maps.model.Marker
import java.time.Duration
import java.time.LocalTime
import kotlin.math.roundToLong

/**
 * Solves Traveling Salesman Problem by creating a distance matrix
 * and using different algorithms (only one available now, TODO).
 * First element in ArrayList<Marker> is the starting point.
 * @author Artem Serediuk
 */
class TSP(private val markers : ArrayList<MarkerParcelable>) {
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
     * Solves TSP problem using nearest neighbor algorithm and returns path
     */
    fun calculateNearestNeighbor() : ArrayList<MarkerParcelable> {
        val path = arrayListOf<MarkerParcelable>()
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
    companion object {
        /**
         * Calculates distances in meters between 2 LatLng points
         */
        private fun calculateDistance(markerA : MarkerParcelable, markerB : MarkerParcelable) : Float {
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

        fun createSchedule(
            uav: UavEntity,
            path: ArrayList<MarkerParcelable>,
            departureTimeStart: LocalTime,
            timeMonitoring: Duration,
            timeCharging: Duration,
            abrasSpeed: Int
        ): ArrayList<ResultData> {
            //path.add(path.last()) //add last element to end
            val result = arrayListOf<ResultData>()

            result.add(
                ResultData(
                    path[0],
                    false,
                    departureTimeStart,
                    departureTimeStart,
                    Duration.ZERO,
                    Duration.ofMinutes(uav.flightTime!!.toLong())
                )
            )
            for(i in 1 until path.size) {
                //todo should test this, it might be wrong
                val distance = calculateDistance(path[i], path[i-1])

                val flightTime = Duration.ofSeconds( //3.6 is a divider for km/h to m/s
                    (distance / (uav.speed!! / 3.6)).roundToLong()
                )
                //Log.i("TSP", "[$i] Distance: $distance FlightTime: $flightTime")

                //Log.e("TSP", "Point [$i] Flight time: ${flightTime.seconds}s Arrival: $arrivalTime")
                val arrivalTime = result[i-1].arrivalTime!! + flightTime + result[i-1].timeSpent
                //determine if this point is a PBR
                //calculate distance between this and next point
                var isPBR = false
                var timeLeft = result[i-1].batteryTimeLeft!! - timeMonitoring
                if(timeLeft.isNegative || timeLeft.isZero) {
                    timeLeft = Duration.ofMinutes(uav.flightTime!!.toLong())
                    isPBR = true
                }

                val timeSpent = if(isPBR) {
                    timeMonitoring + timeCharging
                } else {
                    timeMonitoring
                }

                val departureTime = arrivalTime + timeSpent

                result.add(
                    ResultData(
                        path[i],
                        isPBR,
                        arrivalTime,
                        departureTime,
                        timeSpent,
                        timeLeft
                    )
                )
                Log.i("TSP", result[i].toString())
            }

            return result
        }
    }

}