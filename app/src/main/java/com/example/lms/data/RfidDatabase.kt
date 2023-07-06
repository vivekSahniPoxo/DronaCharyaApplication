package com.example.lms.data

import android.content.Context

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase



@Database(entities = [Rfid::class,BookDetailsModel::class], version = 2, exportSchema = false)

abstract class RfidDatabase : RoomDatabase() {

    abstract fun rfidDao(): RfidDao

    companion object {
        @Volatile
        private var INSTANCE: RfidDatabase? = null

        fun getDatabase(context: Context): RfidDatabase {
            val tempInstance = INSTANCE
            if(tempInstance != null){
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RfidDatabase::class.java,
                    "rfid_database")
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }


}