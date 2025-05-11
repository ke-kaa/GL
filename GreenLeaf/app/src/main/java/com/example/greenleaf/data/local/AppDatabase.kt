package com.example.greenleaf.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.greenleaf.data.local.dao.ObservationDao
import com.example.greenleaf.data.local.dao.PlantDao
import com.example.greenleaf.data.local.dao.UserDao
import com.example.greenleaf.data.local.entities.ObservationEntity
import com.example.greenleaf.data.local.entities.PlantEntity
import com.example.greenleaf.data.local.entities.UserEntity
import javax.inject.Singleton

@Database(
    entities = [
        UserEntity::class,
        PlantEntity::class,
        ObservationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun UserDao(): UserDao
    abstract fun plantDao(): PlantDao
    abstract fun observationDao(): ObservationDao
    
    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "greenleaf_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
} 