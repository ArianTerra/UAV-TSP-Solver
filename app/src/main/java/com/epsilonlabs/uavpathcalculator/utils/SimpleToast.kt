package com.epsilonlabs.uavpathcalculator.utils

import android.content.Context
import android.widget.Toast

class SimpleToast {
    companion object {
        fun show(context: Context, string: String) {
            Toast.makeText(context, string, Toast.LENGTH_SHORT).show()
        }
    }
}