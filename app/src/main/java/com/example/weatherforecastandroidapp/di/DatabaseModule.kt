package com.example.weatherforecastandroidapp.di

import android.content.Context
import androidx.room.Room
import com.example.weatherforecastandroidapp.data.local.AppDatabase
import com.example.weatherforecastandroidapp.data.local.SavedPlaceDao
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "weather_forecast.db").build()

    @Provides
    fun provideSavedPlaceDao(database: AppDatabase): SavedPlaceDao = database.savedPlaceDao()
}
