package com.example.pomocnysasiad.model

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.core.content.ContextCompat
import android.os.Bundle

object LocationService  {
    var myLocation: Location = Location("")

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