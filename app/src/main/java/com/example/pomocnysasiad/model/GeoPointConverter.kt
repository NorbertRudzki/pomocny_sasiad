package com.example.pomocnysasiad.model

import androidx.room.TypeConverter
import com.google.firebase.firestore.GeoPoint
import com.google.gson.Gson

class GeoPointConverter {
    @TypeConverter
    fun stringToGeoPoint(data: String): GeoPoint = Gson().fromJson(data, GeoPoint::class.java)

    @TypeConverter
    fun geoPointToString(geoPoint: GeoPoint): String = Gson().toJson(geoPoint)
}