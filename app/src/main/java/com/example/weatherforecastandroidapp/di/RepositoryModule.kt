package com.example.weatherforecastandroidapp.di

import com.example.weatherforecastandroidapp.data.repository.PlacesRepository
import com.example.weatherforecastandroidapp.data.repository.PlacesRepositoryImpl
import com.example.weatherforecastandroidapp.data.repository.WeatherRepository
import com.example.weatherforecastandroidapp.data.repository.WeatherRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(impl: WeatherRepositoryImpl): WeatherRepository

    @Binds
    @Singleton
    abstract fun bindPlacesRepository(impl: PlacesRepositoryImpl): PlacesRepository
}
