package com.example.pomocnysasiad.fragment

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import com.example.pomocnysasiad.R
import com.example.pomocnysasiad.model.LocationService
import com.example.pomocnysasiad.model.MyPreference
import com.example.pomocnysasiad.model.Request
import com.example.pomocnysasiad.viewmodel.RequestViewModel
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.fragment_search_request.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SearchRequestFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private val requestViewModel by viewModels<RequestViewModel>()
    private val markerRequestsMap = HashMap<Marker, Request>()
    private lateinit var preferences: MyPreference
    private lateinit var locationService: LocationService
    private lateinit var locationLiveData: LiveData<Location?>
    var mMap: GoogleMap? = null
    var currentRequests: List<Request>? = listOf()
    val LOCATION_PERMISSION = 30

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_request, container, false)
    }

    override fun onResume() {
        super.onResume()
        Log.d("life","resumed")
        if (preferences.getLocation().latitude == Location("").latitude
            && preferences.getLocation().longitude == Location("").longitude
        ) {
            Log.d("lokalizacja","brak zapisanej lokalizacji")
            checkPermissionAndSetLocation()
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("life","created")
        preferences = MyPreference(requireContext())
        locationService = LocationService(requireContext())
        mapView.onCreate(savedInstanceState)
        mapView.onResume()



        Log.d("saved loc", (preferences.getLocation()).toString())

        MapsInitializer.initialize(requireActivity().applicationContext)
        mapView.getMapAsync(this)

        readLocation.setOnClickListener {
            refreshMap()
            checkPermissionAndSetLocation()
        }

        requestViewModel.getAllRequests().observe(viewLifecycleOwner) {
            if (it != null) {
                currentRequests = it
                refreshMap()
            }
        }

    }
    private fun checkPermissionAndSetLocation(){
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val locationManager =
                requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Log.d("checkPermissionAndSetLocation","brak GPS")
                AlertDialog.Builder(requireContext()).setTitle("Proszę włączyć GPS")
                    .setPositiveButton("OK") { _: DialogInterface, _: Int ->
                        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }.setNegativeButton("Anuluj") { dialogInterface: DialogInterface, i: Int ->
                        dialogInterface.dismiss()
                    }.create().show()
            } else {
                findLocationAndMoveCamera()
            }
        } else {
            Log.d("checkPermissionAndSetLocation","brak uprawnien")
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), LOCATION_PERMISSION
            )
        }
    }

    private fun refreshMap() {
        mMap?.clear()
        locationDisplay.text =
            "current location:\nlatitude = ${preferences.getLocation().latitude} \nlongitude = ${preferences.getLocation().longitude}"

        currentRequests?.let {
            showPins(it)
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap
        mMap?.setOnMarkerClickListener(this)
        Log.d("init location", preferences.getLocation().toString())
        mMap?.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    preferences.getLocation().latitude,
                    preferences.getLocation().longitude
                ), 11f
            )
        )

        currentRequests?.let {
            showPins(it)
        }
    }

    private fun findLocationAndMoveCamera() {
        locationLiveData = locationService.getLocation()
        locationLiveData.observe(viewLifecycleOwner) {
            if (it != null && it.latitude != Location("").latitude && it.longitude != Location("").longitude) {
                preferences.setLocation(it)
                mMap?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            preferences.getLocation().latitude,
                            preferences.getLocation().longitude
                        ), 11f
                    )
                )
                locationLiveData.removeObservers(viewLifecycleOwner)
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    locationLiveData.removeObservers(viewLifecycleOwner)
                    delay(2000)
                    findLocationAndMoveCamera()
                }
            }
        }
    }

    private fun showPins(requests: List<Request>) {
        mMap?.let {
            markerRequestsMap.clear()
            for (req in requests) {
                val marker = it.addMarker(
                    MarkerOptions()
                        .position(LatLng(req.location.latitude, req.location.longitude))
                        .title(req.title)
                )
                markerRequestsMap[marker] = req
            }

        }
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        Log.d("kliknieto", "marker")
        marker?.let {
            findNavController().navigate(
                SearchRequestFragmentDirections.actionSearchRequestFragment2ToRequestDetails().actionId,
                bundleOf(
                    "title" to markerRequestsMap[marker]!!.title,
                    "auth" to markerRequestsMap[marker]!!.userInNeedName,
                    "desc" to markerRequestsMap[marker]!!.description
                )
            )
        }

        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (requestCode == LOCATION_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val locationManager =
                    requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    AlertDialog.Builder(requireContext()).setTitle("Proszę włączyć GPS")
                        .setPositiveButton("OK") { _: DialogInterface, _: Int ->
                            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        }.setNegativeButton("Anuluj") { dialogInterface: DialogInterface, i: Int ->
                            dialogInterface.dismiss()
                        }.create().show()
                } else {
                    locationService = LocationService(requireContext())
                    findLocationAndMoveCamera()
                }
            }
        }
    }

}


