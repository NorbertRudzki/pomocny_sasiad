package com.example.pomocnysasiad.model

import android.content.Context
import android.location.Location

class MyPreference(context: Context) {
    val PREFERENCE_NAME = "my preference"
    val PREF_ROLE = "role"          //Int:   1 = volunteer   2 = in need   else = unknown
    val PREF_LATITUDE = "latitude"
    val PREF_LONGITUDE = "longitude"
    val PREF_RANGE = "range"

    val preference = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    val editor = preference.edit()

    fun getRole() : Int{
        return preference.getInt(PREF_ROLE, 0)
    }

    fun setRole(role : Int){
        editor.putInt(PREF_ROLE, role)
        editor.apply()
    }

    fun getRange(): Float = preference.getFloat(PREF_RANGE, 1.0f)

    fun setRange(range: Float){
        editor.putFloat(PREF_RANGE, range)
        editor.apply()
    }

    fun getLocation() : Location {
        val lat = preference.getFloat(PREF_LATITUDE, 0f)
        val lng = preference.getFloat(PREF_LONGITUDE, 0f)
        val location = Location("")
        location.latitude = lat.toDouble()
        location.longitude = lng.toDouble()
        return location
    }

    fun setLocation(location : Location){
        editor.putFloat(PREF_LATITUDE, location.latitude.toFloat())
        editor.putFloat(PREF_LONGITUDE, location.longitude.toFloat())
        editor.apply()
    }
}