package com.example.pomocnysasiad.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Chat::class,
    Message::class,
    Request::class,
    Product::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(GeoPointConverter::class)
abstract class LocalDatabase: RoomDatabase() {
    abstract fun requestDao(): RequestDao
    abstract fun productDao(): ProductDao
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao

    companion object {
        private var instance: LocalDatabase? = null

        fun getInstance(context: Context): LocalDatabase? {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context,
                    LocalDatabase::class.java,
                    "pomocnySasiadDatabase"
                )
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return instance
        }
    }
}