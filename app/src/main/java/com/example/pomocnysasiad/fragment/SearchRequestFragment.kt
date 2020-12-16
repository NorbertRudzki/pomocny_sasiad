package com.example.pomocnysasiad.fragment

import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.pomocnysasiad.R
import com.example.pomocnysasiad.model.LocationService
import com.example.pomocnysasiad.model.Request
import com.example.pomocnysasiad.viewmodel.RequestViewModel
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.fragment_search_request.*


class SearchRequestFragment : Fragment(), OnMapReadyCallback {
    private val requestViewModel by viewModels<RequestViewModel>()
    var mMap: GoogleMap? = null
    var currentRequests: List<Request>? = listOf(
        Request(1, "", "", "testN", "", "", GeoPoint(52.0, 19.0), 0),
        Request(2, "", "", "testE", "", "", GeoPoint(51.5, 19.5), 0),
        Request(3, "", "", "testS", "", "", GeoPoint(51.0, 19.0), 0),
        Request(4, "", "", "testW", "", "", GeoPoint(51.5, 18.5), 0)
    )

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

        readLocation.setOnClickListener { refreshMap() }
        requestViewModel.getAllRequests().observe(viewLifecycleOwner) {
            if (it != null) {
                currentRequests = it
                refreshMap()
            }
        }

      //  todo w taki sposob przechodzi się do innego fragmentu i przekazuje wartość w bundle jako mapa key - value
        //todo a tak się odbiera:   val value = arguments?.get("key")
     //   findNavController().navigate(
     //       SearchRequestFragmentDirections.actionSearchRequestFragment2ToRequestDetails().actionId,
     //       bundleOf("key" to "value")
     //   )
    }

    fun refreshMap() {
        mMap?.setMinZoomPreference(8f)
        locationDisplay.text =
            "current location:\nlatitude = ${LocationService.myLocation.latitude} \nlongitude = ${LocationService.myLocation.longitude}"
        mMap?.moveCamera(
            CameraUpdateFactory.newLatLng(
                LatLng(
                    LocationService.myLocation.latitude,
                    LocationService.myLocation.longitude
                )
            )
        )
        mMap?.addMarker(
            MarkerOptions()
                .position(LatLng(51.0, 19.0))
                .title("My test pin")
        )
        currentRequests?.let {
            showPins(it)
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap
        mMap?.moveCamera(
            CameraUpdateFactory.newLatLng(
                LatLng(
                    LocationService.myLocation.latitude,
                    LocationService.myLocation.longitude
                )
            )
        )
        currentRequests?.let {
            showPins(it)
        }
    }

    fun showPins(requests: List<Request>) {
        mMap?.let {
            for (req in requests) {
                it.addMarker(
                    MarkerOptions()
                        .position(LatLng(req.location.latitude, req.location.longitude))
                        .title(req.title)
                )
            }

        }
    }


}