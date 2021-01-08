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
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.example.pomocnysasiad.R
import com.example.pomocnysasiad.model.*
import com.example.pomocnysasiad.view.CategoryAdapter
import com.example.pomocnysasiad.view.OnCategorySelected
import com.example.pomocnysasiad.view.ProductsListCreator
import com.example.pomocnysasiad.viewmodel.CreateRequestViewModel
import com.example.pomocnysasiad.viewmodel.ProductsListViewModel
import com.example.pomocnysasiad.viewmodel.RequestViewModel
import com.example.pomocnysasiad.viewmodel.UserViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.fragment_create_request.*
import kotlinx.coroutines.*
import kotlin.math.floor
import kotlin.random.Random


class CreateRequestFragment : Fragment(), OnCategorySelected {
    private val userVM by viewModels<UserViewModel>()
    private val requestVM by viewModels<RequestViewModel>()
    private val productsVM by viewModels<ProductsListViewModel>()
    private lateinit var preferences: MyPreference
    private lateinit var locationService: LocationService
    private lateinit var interfaceVM: CreateRequestViewModel
    private var currentUser: User? = null
    private var selectedCategory: String? = null
    private var productsListCreator: ProductsListCreator? = null
    private var currentProductsList: List<Product>? = null
    val LOCATION_PERMISSION = 30

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        interfaceVM = ViewModelProvider(requireActivity()).get(CreateRequestViewModel::class.java)
    }

    override fun onResume() {
        super.onResume()
        if (interfaceVM.isTriedToSend().value != null && interfaceVM.isTriedToSend().value!!) {
            findLocationAndSendRequest()
            interfaceVM.setTriedToSend(false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_request, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("created", "view")
        Log.d("viewmodel", interfaceVM.toString())
        preferences = MyPreference(requireContext())

        userVM.user.observe(viewLifecycleOwner) {
            if (it != null) {
                currentUser = it
            }
        }
        createRequestName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                interfaceVM.setTitle(createRequestName.text.toString())
            }
        }
        createRequestDescription.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                interfaceVM.setDescription(createRequestDescription.text.toString())
            }
        }
        createRequestCategoryRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        createRequestCategoryRecycler.setItemViewCacheSize(Category.categoryList.size)

        interfaceVM.getShoppingList().value?.let { productsVM.addProducts(it) }

        setupRecycler(interfaceVM.getCategory().value)
        interfaceVM.getCategory().value?.let { onCategorySelected(it) }
       // if (interfaceVM.getCategory().value == 0) {
       //     onCategorySelected(0)
       // }
        createRequestName.setText(interfaceVM.getTitle().value)
        createRequestDescription.setText(interfaceVM.getDescription().value)

        createRequestCreateBT.setOnClickListener {
            checkPermissionAndCreateRequest()
        }

    }

    private fun checkPermissionAndCreateRequest() {
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
                Log.d("checkPermissionAndSetLocation", "brak GPS")
                AlertDialog.Builder(requireContext()).setTitle("Proszę włączyć GPS")
                    .setPositiveButton("OK") { _: DialogInterface, _: Int ->
                        interfaceVM.setTriedToSend(true)
                        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }.setNegativeButton("Anuluj") { dialogInterface: DialogInterface, _: Int ->
                        dialogInterface.dismiss()
                    }.create().show()
            } else {
                locationService = LocationService(requireContext())
                findLocationAndSendRequest()
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

    override fun onCategorySelected(position: Int) {
        interfaceVM.setCategory(position)
        selectedCategory = Category.categoryList[position]["name"] as String
        setupRecycler(position)
        if (Category.categoryList[position]["name"].toString() == "Zakupy" && productsListCreator == null) {
            createRequestShoppingList.visibility = View.VISIBLE
            productsListCreator =
                ProductsListCreator(createRequestShoppingList, requireActivity(), productsVM)
            productsListCreator!!.drawListOfProducts(emptyList())
            productsVM.getProducts().observe(viewLifecycleOwner) {
                if (it != null) {
                    interfaceVM.setShoppingList(it)
                    currentProductsList = it
                    productsListCreator!!.drawListOfProducts(it)
                }
            }
        } else if (productsListCreator != null) {
            productsVM.getProducts().removeObservers(viewLifecycleOwner)
            productsListCreator = null
            currentProductsList = null
            createRequestShoppingList.visibility = View.GONE
        }
    }


    private fun setupRecycler(selectedCategory: Int?) {
        createRequestCategoryRecycler.adapter =
            CategoryAdapter(requireContext(), Category.categoryList, this, selectedCategory)
        selectedCategory?.let {
            val smoothScroller = object : LinearSmoothScroller(requireContext()) {
                override fun getHorizontalSnapPreference(): Int {
                    return SNAP_TO_START
                }
            }
            smoothScroller.targetPosition = selectedCategory
            createRequestCategoryRecycler.layoutManager?.startSmoothScroll(smoothScroller)
        }
    }

    private fun createRequest(loc: Location): Request? {
        //todo upewnij sie, ze name zostalo ustawione w profilu
        val userId = currentUser!!.id
        val username = currentUser!!.name
        val title = createRequestName.text.toString()
        val description = createRequestDescription.text.toString()
        val category = selectedCategory
        val location =
            GeoPoint(loc.latitude, loc.longitude)
        val longitudeZone = floor(loc.longitude).toInt()
        when {
            category == null -> showAlert(resources.getString(R.string.empty_request_category))
            description.isBlank() -> showAlert(resources.getString(R.string.empty_request_description))
            title.isBlank() -> showAlert(resources.getString(R.string.empty_request_title))
            else -> return Request(
                userInNeedId = userId,
                userInNeedName = username!!,
                title = title,
                description = description,
                category = category,
                location = location,
                longitudeZone = longitudeZone
            )
        }
        return null
    }

    private fun findLocationAndSendRequest() {
        locationService = LocationService(requireContext())
        val locationLiveData = locationService.getLocation()
        locationLiveData.observe(viewLifecycleOwner) { location ->
            if (location != null && location.latitude != Location("").latitude && location.longitude != Location(
                    ""
                ).longitude
            ) {
                interfaceVM.setTriedToSend(false)
                Log.d("ustawiona lokalizacja", location.toString())
                currentUser?.let { user ->
                    if (user.tokens > 0) {
                        val request = createRequest(location)
                        request?.let { req ->
                            requestVM.insertRequest(req)
                            userVM.decreaseToken()
                            interfaceVM.setTitle("")
                            interfaceVM.setDescription("")
                            interfaceVM.setCategory(null)
                            currentProductsList?.let { list ->
                                currentProductsList!!.forEach { it.listId = req.id }
                                requestVM.insertShoppingListForRequest(
                                    req,
                                    ProductsListWrapper(list)
                                )
                                interfaceVM.setShoppingList(null)
                            }
                            findNavController().navigate(CreateRequestFragmentDirections.actionCreateRequestFragmentToMyRequestsFragment().actionId)
                        }
                    }
                }

                locationLiveData.removeObservers(viewLifecycleOwner)
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    Log.d("nie znaleziono lokalizacji", "probuj jeszcze raz")
                    locationLiveData.removeObservers(viewLifecycleOwner)
                    delay(2000)
                    findLocationAndSendRequest()
                }
            }
        }
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
                    findLocationAndSendRequest()
                }
            }
        }
    }

    private fun showAlert(message: String) {
        AlertDialog.Builder(requireActivity())
            .setTitle(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
    }


}