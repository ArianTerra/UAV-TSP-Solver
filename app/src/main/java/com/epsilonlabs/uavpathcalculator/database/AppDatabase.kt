package com.epsilonlabs.uavpathcalculator.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.epsilonlabs.uavpathcalculator.database.dao.UavDao
import com.epsilonlabs.uavpathcalculator.database.entities.UavEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * RoomDatabase class
 */
@Database(entities = [UavEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun uavDao() : UavDao

    /**
     * Room database callback for populating db
     * TODO remake this to populate db from file
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
            //create and add new object to db here
            var uav = UavEntity(0, "Bayraktar", 10.0, 30)
            uavDao.insert(uav)
            uav = UavEntity(1, "MEGA bayraktar", 5.0, 30)
            uavDao.insert(uav)
            uav = UavEntity(2, "Destroyer", 50.0, 300)
            uavDao.insert(uav)
            uav = UavEntity(3, "Abobas", 50.0, 300)
            uavDao.insert(uav)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE : AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope) : AppDatabase {
            return INSTANCE?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database.db"
                )//.createFromAsset("app_database.db").build()
                    .addCallback(UavDatabaseCallback(scope)).build()
                INSTANCE = instance
                //return instance
                instance
            }
        }

    }
}