package com.epsilonlabs.uavpathcalculator.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.epsilonlabs.uavpathcalculator.ioThread

@Database(entities = [UAV::class], version = 1)
abstract class UAVDatabase : RoomDatabase() {
    abstract fun UAVDao() : UAVDao

    companion object {
        @Volatile private var INSTANCE: UAVDatabase? = null

        fun getInstance(context: Context): UAVDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context.applicationContext,
                UAVDatabase::class.java, "Sample.db")
                // prepopulate the database after onCreate was called
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                                        // insert the data on the IO Thread
                        ioThread {
                            getInstance(context).UAVDao().insertUAV(PREPOPULATE_DATA)
                        }
                    }
                }).build()

        val PREPOPULATE_DATA = listOf(
            UAV(0, "Bayraktar", 100.0, 100)
        )

    }
}