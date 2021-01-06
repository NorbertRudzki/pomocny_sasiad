package com.example.pomocnysasiad.model

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.core.content.ContextCompat
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import kotlin.math.cos
import kotlin.math.floor

class LocationService( private val context: Context)  {
    var myLocation: Location = Location("")
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    init {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val locationRequest = LocationRequest.create()
            locationRequest.interval = 60000
            locationRequest.fastestInterval = 5000
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

            val mLocationCallBack = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    if (locationResult != null
                        && locationResult.locations.isNotEmpty()
                        && locationResult.locations[0].accuracy < 100
                    ) {
                        myLocation = locationResult.locations[0]
                    }
                }
            }
            fusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallBack, null)
        }

    }

    fun getLongZone(range: Double): ArrayList<Int> {
        val zoneLength : Double = 40075.014 * cos(myLocation.latitude)
        val rangeInDegrees = range / zoneLength;
        val position1 = floor(myLocation.longitude - rangeInDegrees).toInt()
        val position2 = floor(myLocation.longitude + rangeInDegrees).toInt()
        val array = ArrayList<Int>()
        array.add(position1)
        if(position2 != position1){
            array.add(position2)
        }
        return array
    }

    fun getLocation(): LiveData<Location?>{
        val result = MutableLiveData<Location?>()

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener {
                result.postValue(it)
                if(it != null){
                    myLocation = it
                }
            }
        }

        return result
    }

}