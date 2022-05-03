package com.epsilonlabs.uavpathcalculator.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * RoomDatabase class
 */
@Database(entities = [Uav::class], version = 1)
abstract class UavDatabase : RoomDatabase() {
    abstract fun uavDao() : UavDao

    /**
     * Room database callback for populating db
     */
    private class UavDatabaseCallback(private val scope: CoroutineScope)
        : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.uavDao())
                }
            }
        }
        suspend fun populateDatabase(uavDao: UavDao) {
            //delete content after app restart
            uavDao.deleteAll()

            val uav = Uav(0, "Bayraktar", 10.0, 30)
            uavDao.insert(uav)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE : UavDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope) : UavDatabase {
            return INSTANCE?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UavDatabase::class.java,
                    "uav_database"
                ).addCallback(UavDatabaseCallback(scope)).build()
                INSTANCE = instance
                //return instance
                instance
            }
        }

    }
}