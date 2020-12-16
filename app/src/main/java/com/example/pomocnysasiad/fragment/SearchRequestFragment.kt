package com.example.pomocnysasiad.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.pomocnysasiad.R
import com.example.pomocnysasiad.model.LocationService
import com.example.pomocnysasiad.model.Request
import com.example.pomocnysasiad.viewmodel.RequestViewModel
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.fragment_search_request.*


class SearchRequestFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {

    var map: GoogleMap? = null
    val cloudVM by viewModels<RequestViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_request, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        MapsInitializer.initialize(requireActivity().applicationContext)
        mapView.getMapAsync(this)



        readLocation.setOnClickListener {

            locationDisplay.text =
                "current location:\nlatitude = ${LocationService.myLocation.latitude} \nlongitude = ${LocationService.myLocation.longitude}"
            map?.moveCamera(
                CameraUpdateFactory.newLatLng(
                    LatLng(
                        LocationService.myLocation.latitude,
                        LocationService.myLocation.longitude
                    )
                )
            )

        }
    }


   override fun onMapReady(googleMap: GoogleMap?) {
       if (ActivityCompat.checkSelfPermission(
               requireContext(),
               Manifest.permission.ACCESS_FINE_LOCATION
           ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
               requireContext(),
               Manifest.permission.ACCESS_COARSE_LOCATION
           ) != PackageManager.PERMISSION_GRANTED
       ) {
           Log.d("brak","uprawnien")
           // TODO: Consider calling
           //    ActivityCompat#requestPermissions
           // here to request the missing permissions, and then overriding
           //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
           //                                          int[] grantResults)
           // to handle the case where the user grants the permission. See the documentation
           // for ActivityCompat#requestPermissions for more details.
           return
       }
       map = googleMap
       map?.setMinZoomPreference(12f)
       map?.isMyLocationEnabled = true
       map?.setOnMyLocationButtonClickListener(this)
       map?.setOnMyLocationClickListener(this)
       map?.moveCamera(CameraUpdateFactory.newLatLng(LatLng(LocationService.myLocation.latitude,LocationService.myLocation.longitude)))
   }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(requireContext(), "MyLocation button clicked", Toast.LENGTH_SHORT)
            .show()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }

    override fun onMyLocationClick(location: Location) {
        Log.d("My location", location.toString())

    }


}