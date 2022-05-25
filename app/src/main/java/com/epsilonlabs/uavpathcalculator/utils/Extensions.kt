package com.epsilonlabs.uavpathcalculator.utils

fun Array<FloatArray>.copy() = map { it.clone() }.toTypedArray()
