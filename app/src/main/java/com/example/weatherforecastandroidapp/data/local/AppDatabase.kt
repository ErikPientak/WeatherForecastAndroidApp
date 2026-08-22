package com.example.weatherforecastandroidapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SavedPlaceEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun savedPlaceDao(): SavedPlaceDao
}
