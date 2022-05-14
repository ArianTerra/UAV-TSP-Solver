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
    val scheduleABRAS: ArrayList<ResultData>
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
                        arrival = point.arrivalTime!! - timeCharging + timeMonitoring
                        //add new record to schedule before PBR1
                        val departureDH = arrival - Duration.ofSeconds(
                            (MapUtils.calculateDistance(DH, point.marker) / (abrasSpeed / 3.6)).roundToLong()
                        )
                        arr.add(ResultData(DH, false, "ABRAS", departureTimeStart, departureDH, Duration.ZERO, Duration.ZERO))
                        //time spent at PBR1
                        timeSpent = Duration.between(arrival, point.departureTime!!) + timeCharging
                        departure = point.departureTime + timeCharging
                    } else
                    {
                        arrival = arr[arr.size - 1].departureTime!! + Duration.ofSeconds(
                                (MapUtils.calculateDistance(point.marker, arr[arr.size - 1].marker) /
                                        (abrasSpeed / 3.6)).roundToLong())
                        departure = point.departureTime!! + timeCharging
                        timeSpent = Duration.between(arrival, departure)

                    }
                    arr.add(ResultData(point.marker, true, "ABRAS", arrival, departure, timeSpent, null))
                }
            }
            //returning back to DH
            if(arr.size > 0) {
                val flightTimeToDH = Duration.ofSeconds(
                    (MapUtils.calculateDistance(arr.last().marker, DH) / (abrasSpeed / 3.6)).roundToLong()
                )
                val arrival = arr.last().departureTime!! + flightTimeToDH
                arr.add(ResultData(
                    DH,
                    false,
                    "ABRAS",
                    arrival,
                    arrival,
                    Duration.ZERO,
                    Duration.ZERO
                ))
            }
            return arr
        }
    val schedule: ArrayList<ResultData>
        get() {
            val result = arrayListOf<ResultData>()
            val ABRAS = scheduleABRAS.filter { abras -> abras.isPBR }
            var abrasIndex = 0
            for (uav in scheduleUAV) {
                result.add(uav)
                if(uav.isPBR) {
                    result.add(ABRAS[abrasIndex])
                    abrasIndex++
                }
            }
            return result
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
            val flightTime = calculateFlightTime(path[i], path[i-1])
            //Log.i("TSP", "[$i] Distance: $distance FlightTime: $flightTime")
            //Log.e("TSP", "Point [$i] Flight time: ${flightTime.seconds}s Arrival: $arrivalTime")
            val arrivalTime = scheduleUAV[i-1].arrivalTime!! + flightTime + scheduleUAV[i-1].timeSpent
            //determine if this point is a PBR
            //calculate distance between this and next point
            var isPBR = false
            var timeLeft = scheduleUAV[i-1].batteryTimeLeft!! - timeMonitoring - flightTime
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
        //return to DQ
        val flightTime = calculateFlightTime(path[0], scheduleUAV.last().marker)
        val arrival = scheduleUAV.last().departureTime!! + flightTime
        val timeLeft = scheduleUAV.last().batteryTimeLeft!! - timeMonitoring - flightTime
        val point = ResultData(
            path[0],
            false,
            uav.name,
            arrival,
            null,
            null,
            timeLeft
        )
        scheduleUAV.add(point)
    }
    fun calculateFlightTime(m0: MarkerParcelable, m1: MarkerParcelable) : Duration {
        val distance = MapUtils.calculateDistance(m0, m1)
        val flightTime = Duration.ofSeconds( //3.6 is a divider for km/h to m/s
            (distance / (uav.speed!! / 3.6)).roundToLong()
        )
        // check if path is larger than uav flight time
        if(flightTime.seconds + timeMonitoring.seconds >= uav.flightTime!! * 60) {
            throw IllegalArgumentException("Flight time between point ${m0.title} " +
                    "and ${m1.title} + monitoring time is higher than ${uav.name} maximum flight time " +
                    "of ${uav.flightTime} minutes.")
        }

        return flightTime
    }
}