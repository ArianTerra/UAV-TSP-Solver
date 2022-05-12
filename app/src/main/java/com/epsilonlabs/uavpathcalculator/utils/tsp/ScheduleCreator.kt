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
    val scheduleUAV: ArrayList<ResultData> = arrayListOf<ResultData>()
    val scheduleABRAS:  ArrayList<ResultData>
        get() {
            val arr = arrayListOf<ResultData>()

            var firstPBR = true
            val DH = path[0] //TODO add new type of marker for DH, for now it is DQ

            for(point in scheduleUAV) {
                //return var
                val arrival: LocalTime
                val departure: LocalTime
                val timeSpent: Duration

                if(point.isPBR) {
                    if(firstPBR) {
                        firstPBR = false
                        arrival = point.arrivalTime!! + timeMonitoring - Duration.ofMinutes(2)
                        //add new record to schedule before PBR1
                        val departureDH = arrival - Duration.ofSeconds(
                            (MapUtils.calculateDistance(DH, point.marker) / (abrasSpeed / 3.6)).roundToLong()
                        )
                        arr.add(ResultData(DH, true, "ABRAS", departureTimeStart, departureDH, Duration.ZERO, Duration.ZERO))
                        //time spent at PBR1
                        timeSpent = Duration.between(point.departureTime!!, arrival) + Duration.ofMinutes(2)
                        departure = point.departureTime + Duration.ofMinutes(2)
                    } else
                    {
                        arrival = arr[arr.size - 1].departureTime!! + Duration.ofSeconds(
                                (MapUtils.calculateDistance(point.marker, arr[arr.size - 1].marker) /
                                        (abrasSpeed / 3.6)).roundToLong())
                        departure = point.departureTime!! + Duration.ofMinutes(2)
                        timeSpent = Duration.between(departure, arrival)

                    }
                    arr.add(ResultData(point.marker, true, "ABRAS", arrival, departure, timeSpent, Duration.ZERO))
                }
            }

            return arr
        }
    val hasPBR: Boolean = scheduleUAV.any { value -> value.isPBR }

    init {
        val start = ResultData(
            path[0],
            false,
            uav.name!!,
            departureTimeStart,
            departureTimeStart,
            Duration.ZERO,
            Duration.ofMinutes(uav.flightTime!!.toLong())
        )
        scheduleUAV.add(start)
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
            val arrivalTime = scheduleUAV[i-1].arrivalTime!! + flightTime + scheduleUAV[i-1].timeSpent
            //determine if this point is a PBR
            //calculate distance between this and next point
            var isPBR = false
            var timeLeft = scheduleUAV[i-1].batteryTimeLeft!! - timeMonitoring
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
                uav.name!!,
                arrivalTime,
                departureTime,
                timeSpent,
                timeLeft
            )

            scheduleUAV.add(point)
            Log.i("TSP", scheduleUAV[i].toString())
        }
    }
}