package com.epsilonlabs.uavpathcalculator.utils.tsp

import com.epsilonlabs.uavpathcalculator.utils.MarkerParcelable

class TspDynamicProgramming : TSP() {
    override fun calculatePath(
        markers: ArrayList<MarkerParcelable>,
        matrix: Array<FloatArray>
    ): ArrayList<MarkerParcelable> {
        val path = arrayListOf<MarkerParcelable>()
        val result = DPtsp(matrix).tour
        val pathIndexes = result.slice(0 until result.size - 1)

        for(i in pathIndexes) {
            path.add(markers[i])
        }

        return path
    }

    override fun toString(): String {
        return "Dynamic Programming"
    }
}