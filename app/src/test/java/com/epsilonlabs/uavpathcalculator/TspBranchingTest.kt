package com.epsilonlabs.uavpathcalculator

import com.epsilonlabs.uavpathcalculator.utils.MarkerParcelable
import com.epsilonlabs.uavpathcalculator.utils.tsp.TspBranching
import org.junit.Test
import org.junit.Assert
import org.junit.Assert.assertArrayEquals

class TspBranchingTest {
    @Test
    fun calculatePath_size5_02314() {
        val fakeMarkers = arrayListOf<MarkerParcelable>(
            MarkerParcelable("0", 0f.toDouble(), 0f.toDouble()),
            MarkerParcelable("1", 0f.toDouble(), 0f.toDouble()),
            MarkerParcelable("2", 0f.toDouble(), 0f.toDouble()),
            MarkerParcelable("3", 0f.toDouble(), 0f.toDouble()),
            MarkerParcelable("4", 0f.toDouble(), 0f.toDouble())
        )
        val matrix = arrayOf(
            floatArrayOf(Float.POSITIVE_INFINITY, 10f, 8f, 9f, 7f),
            floatArrayOf(10f, Float.POSITIVE_INFINITY, 10f, 5f, 6f),
            floatArrayOf(8f, 10f, Float.POSITIVE_INFINITY, 8f, 9f),
            floatArrayOf(9f, 5f, 8f, Float.POSITIVE_INFINITY, 6f),
            floatArrayOf(7f, 6f, 9f, 6f, Float.POSITIVE_INFINITY)
        )
        val correct = arrayListOf<MarkerParcelable>(
            fakeMarkers[0], fakeMarkers[2], fakeMarkers[3], fakeMarkers[1], fakeMarkers[4]
        )
        val result = TspBranching().calculatePath(fakeMarkers, matrix)
        assertArrayEquals(correct.toArray(), result.toArray())
    }
    @Test
    fun calculatePath_size4_0132() {
        val fakeMarkers = arrayListOf<MarkerParcelable>(
            MarkerParcelable("0", 0f.toDouble(), 0f.toDouble()),
            MarkerParcelable("1", 0f.toDouble(), 0f.toDouble()),
            MarkerParcelable("2", 0f.toDouble(), 0f.toDouble()),
            MarkerParcelable("3", 0f.toDouble(), 0f.toDouble())
        )
        val matrix = arrayOf(
            floatArrayOf(Float.POSITIVE_INFINITY, 10f, 15f, 20f),
            floatArrayOf(10f, Float.POSITIVE_INFINITY, 35f, 25f),
            floatArrayOf(15f, 35f, Float.POSITIVE_INFINITY, 30f),
            floatArrayOf(20f, 25f, 30f, Float.POSITIVE_INFINITY)
        )
        val correct = arrayListOf<MarkerParcelable>(
            fakeMarkers[0], fakeMarkers[1], fakeMarkers[3], fakeMarkers[2]
        )
        val result = TspBranching().calculatePath(fakeMarkers, matrix)
        assertArrayEquals(correct.toArray(), result.toArray())
    }
}