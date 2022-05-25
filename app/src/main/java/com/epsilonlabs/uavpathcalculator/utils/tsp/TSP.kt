package com.epsilonlabs.uavpathcalculator.utils.tsp

import com.epsilonlabs.uavpathcalculator.utils.MapUtils
import com.epsilonlabs.uavpathcalculator.utils.MarkerParcelable
import com.epsilonlabs.uavpathcalculator.utils.copy
import java.util.*
import kotlin.collections.ArrayList

/**
 * Solves Traveling Salesman Problem by creating a distance matrix
 * and using different algorithms (only one available now, TODO).
 * First element in ArrayList<Marker> is the starting point.
 * @author Artem Serediuk
 */
abstract class TSP {
    companion object {
        fun calculateDistanceMatrix(markers : ArrayList<MarkerParcelable>) : Array<FloatArray>
        {
            val matrix = Array(markers.size) { FloatArray(markers.size) }
            for(i in 0 until markers.size) {
                for (j in 0 until markers.size) {
                    var distance = MapUtils.calculateDistance(markers[i], markers[j])
                    if(distance == 0.0f) distance = Float.POSITIVE_INFINITY
                    matrix[i][j] = distance
                }
            }
            return matrix
        }
        fun calculatePathLength(markers : ArrayList<MarkerParcelable>): Float {
            var cost = 0f
            for(i in markers.indices) {
                if(i < markers.size - 1) cost += MapUtils.calculateDistance(markers[i], markers[i+1])
                else cost += MapUtils.calculateDistance(markers[i], markers[0])
            }
            return cost
        }
    }
    abstract fun calculatePath(markers : ArrayList<MarkerParcelable>,
                               matrix: Array<FloatArray> = calculateDistanceMatrix(markers)) : ArrayList<MarkerParcelable>
}