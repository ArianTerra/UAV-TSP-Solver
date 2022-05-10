package com.epsilonlabs.uavpathcalculator.utils.tsp

import android.util.Log
import com.epsilonlabs.uavpathcalculator.database.entities.UavEntity
import com.epsilonlabs.uavpathcalculator.utils.MapUtils
import com.epsilonlabs.uavpathcalculator.utils.MarkerParcelable
import java.time.Duration
import java.time.LocalTime
import kotlin.math.roundToLong

class ScheduleCreator(
    private val uav: UavEntity,
    private val path: ArrayList<MarkerParcelable>,
    private val departureTimeStart: LocalTime,
    private val timeMonitoring: Duration,
    private val timeCharging: Duration,
    private val abrasSpeed: Int
) {
    val schedule: ArrayList<ResultData> = arrayListOf<ResultData>()
    val scheduleWithABRAS:  ArrayList<ResultData>
        get() {
            val arr = arrayListOf<ResultData>()
            for(point in schedule) {
                arr.add(point)
                if(point.isPBR) {
                    val arrival = point.arrivalTime!! + timeMonitoring - Duration.ofMinutes(2)
                    TODO()
                }
            }
            return arr
        }

    init {
        val start = ResultData(
            path[0],
            false,
            departureTimeStart,
            departureTimeStart,
            Duration.ZERO,
            Duration.ofMinutes(uav.flightTime!!.toLong())
        )
        schedule.add(start)
        for(i in 1 until path.size) {
            val distance = MapUtils.calculateDistance(path[i], path[i-1])

            val flightTime = Duration.ofSeconds( //3.6 is a divider for km/h to m/s
                (distance / (uav.speed!! / 3.6)).roundToLong()
            )
            // check if path is larger than uav flight time
            if(flightTime.seconds >= uav.flightTime * 60) {
                throw IllegalArgumentException("Flight time between point ${path[i].title} " +
                        "and ${path[i-1].title} is higher than ${uav.name} maximum flight time " +
                        "of ${uav.flightTime} minutes.")
            }
            //Log.i("TSP", "[$i] Distance: $distance FlightTime: $flightTime")
            //Log.e("TSP", "Point [$i] Flight time: ${flightTime.seconds}s Arrival: $arrivalTime")
            val arrivalTime = schedule[i-1].arrivalTime!! + flightTime + schedule[i-1].timeSpent
            //determine if this point is a PBR
            //calculate distance between this and next point
            var isPBR = false
            var timeLeft = schedule[i-1].batteryTimeLeft!! - timeMonitoring
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

            val point = ResultData(
                path[i],
                isPBR,
                arrivalTime,
                departureTime,
                timeSpent,
                timeLeft
            )

            schedule.add(point)
            Log.i("TSP", schedule[i].toString())
        }
    }
}