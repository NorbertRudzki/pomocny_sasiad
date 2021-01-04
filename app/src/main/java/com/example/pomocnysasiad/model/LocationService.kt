package com.example.pomocnysasiad.model

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.core.content.ContextCompat
import android.os.Bundle
import kotlin.math.cos
import kotlin.math.floor

object LocationService  {
    var myLocation: Location = Location("")


    fun getLongZone(range: Double): ArrayList<Int> {
        val zoneLenght : Double = 40075.014 * cos(myLocation.latitude)
        val rangeInDegrees = range / zoneLenght;
        val position1 = floor(myLocation.longitude - rangeInDegrees).toInt()
        val position2 = floor(myLocation.longitude + rangeInDegrees).toInt()
        val array = ArrayList<Int>()
        array.add(position1)
        if(position2 != position1){
            array.add(position2)
        }
        return array
    }





    @SuppressLint("MissingPermission")
    fun initialize(locationManager : LocationManager) {
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000,
                0f,
                object : LocationListener {
                    override fun onLocationChanged(loc: Location) {
                        myLocation = loc;
                    }

                    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
                        //TODO("Not yet implemented")
                    }

                    override fun onProviderEnabled(p0: String?) {
                        //TODO("Not yet implemented")
                    }

                    override fun onProviderDisabled(p0: String?) {
                        //TODO("Not yet implemented")
                    }
                }
        )
    }
}