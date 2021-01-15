package com.example.pomocnysasiad.fragment

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.pomocnysasiad.R
import com.example.pomocnysasiad.VolunteerRequestService
import com.example.pomocnysasiad.model.*
import com.example.pomocnysasiad.viewmodel.RequestViewModel
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_search_request.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SearchRequestFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private lateinit var requestViewModel: RequestViewModel
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
        Log.d("life", "resumed")
        if (preferences.getLocation().latitude == Location("").latitude
            && preferences.getLocation().longitude == Location("").longitude
        ) {
            Log.d("lokalizacja", "brak zapisanej lokalizacji")
            checkPermissionAndSetLocation()
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("life", "created")
        preferences = MyPreference(requireContext())
        locationService = LocationService(requireContext(), preferences.getLocation())
        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        requestViewModel = ViewModelProvider(requireActivity()).get(RequestViewModel::class.java)

        Log.d("saved loc", (preferences.getLocation()).toString())

        MapsInitializer.initialize(requireActivity().applicationContext)
        mapView.getMapAsync(this)

        readLocation.setOnClickListener {
            refreshMap()
            checkPermissionAndSetLocation()
        }
        requestViewModel.getNearbyRequests().observe(viewLifecycleOwner){
            Log.d("Wczytano we fragmencie", it.toString())
            currentRequests = it
            refreshMap()
        }

        if (!VolunteerRequestService.isSearching) {
            val intentService = Intent(requireContext(), VolunteerRequestService::class.java)
            requireContext().stopService(intentService)
            requireContext().startService(intentService)
        }

        requestViewModel.setFilter(
            Filter(
                locationService.getLatZone(preferences.getRange().toDouble()),
                locationService.getLongZone(preferences.getRange().toDouble()),
                locationService.getLongNearbyPoints(preferences.getRange().toDouble())
            )
        )

        rangeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            var range: Double = 1.0
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                range = progress / 2.0
                rangeTextView.text = "${range}km"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                Toast.makeText(requireContext(), range.toString(), Toast.LENGTH_LONG).show()
                requestViewModel.setFilter(
                    Filter(
                        locationService.getLatZone(range),
                        locationService.getLongZone(range),
                        locationService.getLongNearbyPoints(range)
                    )
                )
                preferences.setRange(range.toFloat())
                requireContext().stopService(Intent(requireContext(), VolunteerRequestService::class.java))
                requireContext().startService(Intent(requireContext(), VolunteerRequestService::class.java))
            }
        })
        rangeSeekBar.setProgress(preferences.getRange().toInt() * 2, true)
        rangeTextView.text = "${preferences.getRange()}km"
    }

    private fun checkPermissionAndSetLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap?.isMyLocationEnabled = true
            val locationManager =
                requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Log.d("checkPermissionAndSetLocation", "brak GPS")
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
            Log.d("checkPermissionAndSetLocation", "brak uprawnien")
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
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap?.isMyLocationEnabled = true
        }

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

                requestViewModel.setFilter(
                    Filter(
                        locationService.getLatZone(preferences.getRange().toDouble()),
                        locationService.getLongZone(preferences.getRange().toDouble()),
                        locationService.getLongNearbyPoints(preferences.getRange().toDouble())
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
                            .icon(choseIcon(req.category))
                )
                markerRequestsMap[marker] = req
            }
        }
    }

    private fun choseIcon(category: Int): BitmapDescriptor {
        val bitmap = BitmapFactory.decodeResource(context?.resources, Category.categoryList[category]["icon"] as Int)

        return BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bitmap, 100, 100, false))
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        Log.d("kliknieto", "marker")
        marker?.let {
            findNavController().navigate(
                SearchRequestFragmentDirections.actionSearchRequestFragment2ToRequestDetails().actionId,
                bundleOf(
                    "request" to Gson().toJson(markerRequestsMap[marker]!!),
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
