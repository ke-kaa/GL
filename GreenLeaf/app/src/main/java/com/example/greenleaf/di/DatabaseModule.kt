package com.example.greenleaf.di

import android.content.Context
import com.example.greenleaf.data.local.AppDatabase
import com.example.greenleaf.data.local.dao.ObservationDao
import com.example.greenleaf.data.local.dao.PlantDao
import com.example.greenleaf.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.getDatabase(context)

    @Provides
    fun providePlantDao(db: AppDatabase): PlantDao = db.plantDao()

    @Provides
    fun provideObservationDao(db: AppDatabase): ObservationDao = db.observationDao()

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.UserDao()
}
