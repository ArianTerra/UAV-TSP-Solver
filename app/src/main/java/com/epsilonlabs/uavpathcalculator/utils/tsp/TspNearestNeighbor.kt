package com.epsilonlabs.uavpathcalculator.utils.tsp

import com.epsilonlabs.uavpathcalculator.utils.MarkerParcelable

class TspNearestNeighbor : TSP() {
    override fun calculatePath(
        markers: ArrayList<MarkerParcelable>,
        matrix: Array<FloatArray>
    ): ArrayList<MarkerParcelable> {
        val path = arrayListOf<MarkerParcelable>()
        val pathIndexes = arrayListOf<Int>()
        pathIndexes.add(0)

        for (i in 1 until markers.size) {
            val s = arrayListOf<Float>()
            for (j in 0 until markers.size) {
                s.add(matrix[pathIndexes[i-1]][j])
            }
            pathIndexes.add(s.indexOf(s.minOrNull()))

            for(j in 0 until i) {
                matrix[pathIndexes[i]][pathIndexes[j]] = Float.POSITIVE_INFINITY
            }
        }

        for(i in pathIndexes) {
            path.add(markers[i])
        }

        return path
    }

    override fun toString(): String {
        return "Nearest Neighbor"
    }
}