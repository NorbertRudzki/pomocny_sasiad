package com.example.pomocnysasiad.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.example.pomocnysasiad.R
import com.example.pomocnysasiad.model.LocationService
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.fragment_search_request.*


class SearchRequestFragment : Fragment(), OnMapReadyCallback {

    var mMap: GoogleMap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_request, container, false)
    }

    override fun onStart() {
        super.onStart()
        val mapFragment = mapView as SupportMapFragment
        mapFragment.getMapAsync(this)

        readLocation.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                locationDisplay.text = "current location:\nlatitude = ${LocationService.myLocation.latitude} \nlongitude = ${LocationService.myLocation.longitude}"
                mMap?.moveCamera(CameraUpdateFactory.newLatLng(LatLng(LocationService.myLocation.latitude,LocationService.myLocation.longitude)))
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap
        mMap?.moveCamera(CameraUpdateFactory.newLatLng(LatLng(LocationService.myLocation.latitude,LocationService.myLocation.longitude)))

    }



}