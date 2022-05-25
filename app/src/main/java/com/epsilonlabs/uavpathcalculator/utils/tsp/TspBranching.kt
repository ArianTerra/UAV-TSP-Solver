package com.epsilonlabs.uavpathcalculator.utils.tsp

import com.epsilonlabs.uavpathcalculator.utils.MarkerParcelable
import com.epsilonlabs.uavpathcalculator.utils.copy
import java.util.*
import kotlin.collections.ArrayList

class TspBranching : TSP() {
    override fun calculatePath(
        markers: ArrayList<MarkerParcelable>,
        matrix: Array<FloatArray>
    ): ArrayList<MarkerParcelable> {
        var pathIndexes = arrayListOf<Int>()
        val path = arrayListOf<MarkerParcelable>()

//        val pq = PriorityQueue<BranchNode> {p1: BranchNode, p2: BranchNode ->
//            p2.cost.toInt() - p1.cost.toInt()
//        }
        val pq = PriorityQueue<BranchNode>()
        val indexes = arrayListOf<Pair<Int, Int>>()
        val root = BranchNode(matrix, indexes, 0, -1, 0)
        root.cost = calculateCost(root.reducedMatrix)
        pq.add(root)
        while(!pq.isEmpty()) {
            val min = pq.poll()
            val i = min.vertex
            if(min.level == matrix.size - 1) {
                min.path.add(Pair(i, 0))
                //println(min.path)
                //return min.cost
                pathIndexes = min.path.mapTo(arrayListOf()) { t -> t.first }
                break
            }
            for(j in matrix.indices) {
                if(min.reducedMatrix[i][j] != Float.POSITIVE_INFINITY) {
                    val child = BranchNode(min.reducedMatrix, min.path, min.level+1, i, j)
                    val childMatrixCost = calculateCost(child.reducedMatrix)
                    child.cost = min.cost + min.reducedMatrix[i][j] + childMatrixCost
                    pq.add(child)
                }
            }
        }
        for(i in pathIndexes) {
            path.add(markers[i])
        }
        return path
    }
    private fun calculateCost(matrix: Array<FloatArray>): Float {
        val newMatrix = matrix.copy()
        var cost = 0f
        //by rows
        for(i in newMatrix.indices) {
            val min = newMatrix[i].minOrNull()!!
            if(min != Float.POSITIVE_INFINITY) cost += min
            for(j in newMatrix.indices) {
                if(newMatrix[i][j] == Float.POSITIVE_INFINITY) continue
                newMatrix[i][j] -= min
            }
        }
        //by columns
        for(i in newMatrix.indices) {
            var min = Float.POSITIVE_INFINITY
            for(j in newMatrix.indices) {
                if(newMatrix[j][i] < min) {
                    min = newMatrix[j][i]
                }
            }
            if(min != Float.POSITIVE_INFINITY) cost += min
            for(j in newMatrix.indices) {
                if(newMatrix[j][i] == Float.POSITIVE_INFINITY) continue
                newMatrix[j][i] -= min
            }
        }
        return cost
    }

    private class BranchNode (
        val parentMatrix: Array<FloatArray>,
        var path: ArrayList<Pair<Int, Int>>,
        val level: Int,
        val i: Int,
        val j: Int
    ) : Comparable<BranchNode>{
        var cost: Float = 0f
        var vertex: Int = j

        val reducedMatrix = parentMatrix.copy()

        init {
            path = ArrayList(path)
            if (level != 0) {
                path.add(Pair(i, j))
                for (k in parentMatrix.indices) {
                    reducedMatrix[i][k] = Float.POSITIVE_INFINITY
                    reducedMatrix[k][j] = Float.POSITIVE_INFINITY
                }
            }
            reducedMatrix[j][0] = Float.POSITIVE_INFINITY
        }

        override fun compareTo(other: BranchNode): Int {
            return cost.compareTo(other.cost)
        }

    }

    override fun toString(): String {
        return "Branch and bound"
    }
}