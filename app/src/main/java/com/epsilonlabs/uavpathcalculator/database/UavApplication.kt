package com.epsilonlabs.uavpathcalculator.database

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class UavApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { UavDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { UavRepository(database.uavDao()) }
}