package com.epsilonlabs.uavpathcalculator.utils

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class AlertUtils {
    companion object {
        fun showShortToast(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
        fun showOkAlert(context: Context, title: String, message: String) {
             AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null).create().show()
        }
    }
}