package com.epsilonlabs.uavpathcalculator.utils

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MarkerParcelable(val title: String, val latitude: Double, val longitude: Double) : Parcelable