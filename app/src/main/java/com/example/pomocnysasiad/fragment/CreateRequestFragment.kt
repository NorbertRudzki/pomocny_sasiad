package com.example.pomocnysasiad.fragment

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.opengl.Visibility
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.app.ActivityCompat
import androidx.core.view.children
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import com.example.pomocnysasiad.service.InNeedRequestService
import com.example.pomocnysasiad.R
import com.example.pomocnysasiad.model.*
import com.example.pomocnysasiad.view.*
import com.example.pomocnysasiad.viewmodel.*
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.create_request_checkbox_row.view.*
import kotlinx.android.synthetic.main.create_request_question_row.*
import kotlinx.android.synthetic.main.create_request_question_row.view.*
import kotlinx.android.synthetic.main.fragment_create_request.*
import kotlinx.android.synthetic.main.product_row.view.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.floor


class CreateRequestFragment : Fragment(), OnCategorySelected, OnAnswerSelected {
    private val userVM by viewModels<UserViewModel>()
    private val requestVM by viewModels<RequestViewModel>()
    private val productsVM by viewModels<ProductsListViewModel>()
    private val chatVM by viewModels<ChatViewModel>()
    private lateinit var preferences: MyPreference
    private lateinit var locationService: LocationService
    private lateinit var interfaceVM: CreateRequestViewModel
    private var currentUser: User? = null
    private var selectedCategory: Int? = null
    private var productsListCreator: ProductsListCreator? = null
    private var currentProductsList: List<Product>? = null
    private var currentAnswer: ArrayList<String>? = null
    private var currentForm: ArrayList<View>? = null
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
        if (!InNeedRequestService.isSearching) {
            val intentService = Intent(requireContext(), InNeedRequestService::class.java)
            requireContext().stopService(intentService)
            requireContext().startService(intentService)
        }
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

        //interfaceVM.getForm().value?.let {
        //    currentAnswer = it
        //    Log.d("interfaceVM.getForm()", currentAnswer.toString())
        //}

        setupRecycler(interfaceVM.getCategory().value)
        interfaceVM.getCategory().value?.let { onCategorySelected(it) }
        // if (interfaceVM.getCategory().value == 0) {
        //     onCategorySelected(0)
        // }
        if(interfaceVM.getForm().value == null){
            interfaceVM.setForm(arrayListOf())
        }

        createRequestName.setText(interfaceVM.getTitle().value)
        createRequestDescription.setText(interfaceVM.getDescription().value)

        createRequestCreateBT.setOnClickListener {
            val requestCount = requestVM.getCountOfInNeedRequests()
            requestCount.observe(viewLifecycleOwner) { counter ->
                Log.d("requestCount", counter.toString())
                if (counter < 10) {
                    checkPermissionAndCreateRequest()
                }
                requestCount.removeObservers(viewLifecycleOwner)
            }
        }

    }
    private fun getFormAnswers(): String{
        val stringBuilder = StringBuilder()
        if(currentForm != null){
            for(i in currentForm!!){
                if(i.questionRowSpinner != null){
                    stringBuilder.append(i.questionRowText.text.toString())
                    stringBuilder.append(": ")
                    if(i.questionRowET != null && i.questionRowET.text.toString().isNotBlank()){
                        stringBuilder.append(i.questionRowET.text.toString())

                    } else {
                        stringBuilder.append(i.questionRowSpinner.selectedItem.toString())
                    }
                } else {
                    stringBuilder.append(i.checkBoxRow.text.toString())
                    stringBuilder.append(": ")
                    stringBuilder.append(if(i.checkBoxRow.isChecked) "tak" else "nie")
                }
                stringBuilder.append('\n')
            }
        }
        return  stringBuilder.toString()
    }

    private fun createForm(category: Int) {
        val formOpen = ArrayList<ArrayList<String>>()
        val formCheck = ArrayList<String>()
        createRequestForm.visibility = View.VISIBLE
        createRequestForm.removeAllViewsInLayout()
       // createRequestForm.removeAllViews()
        createRequestForm.descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
        when (category) {
            1 -> {
                //zwierzeta
                formOpen.add(
                    arrayListOf("Preferowana godzina", "rano", "południe", "popołudnie", "wieczór", "Inne(wpisz)")
                )
                formOpen.add(arrayListOf("Gatunek", "Pies", "Kot", "Ptak", "Królik", "Chomik", "Inne(wpisz)"))
                formOpen.add(arrayListOf("Wielkość", "Mały", "Średni", "Duży", "Inne(wpisz)"))
                formOpen.add(arrayListOf("Zadanie", "Spacer", "Dokarmianie", "Weterynarz", "Inne(wpisz)"))
                formCheck.add("Może być niebezpieczny")
            }
            2 -> {
                //smieci
                formOpen.add(arrayListOf("Preferowana godzina", "rano", "południe", "popołudnie", "wieczór", "Inne(wpisz)"))
                formOpen.add(arrayListOf("Typ", "Mieszane", "Butelki", "Duże gabaryty", "Worki", "Inne(wpisz)"))
                formCheck.add("Nie wymaga segregacji/ posegregowane")

            }
            3 -> {
                //rozmowa
                formOpen.add(arrayListOf("Preferowana godzina", "rano", "południe", "popołudnie", "wieczór", "Inne(wpisz)"))
                formOpen.add(arrayListOf("Ile czasu chcesz rozmawiać", "15min", "30min", "45min", "Ile tylko można", "Inne(wpisz)"))
            }
            4 -> {
                //transport
                formOpen.add(arrayListOf("Preferowana godzina", "rano", "południe", "popołudnie", "wieczór", "Inne(wpisz)"))
                formOpen.add(arrayListOf("Co trzeba przetransportować", "Paczkę", "Rzeczy", "Ludzi", "Inne(wpisz)"))
                formOpen.add(arrayListOf("Ilość", "1", "2", "3", "4", "Inne(wpisz)"))
                formOpen.add(arrayListOf("Odległość", "mniej, niż 1km", "między 1km, a 3km", "między 3km, a 5km", "między 5km, a 10km", "Inne(wpisz)"))
                formCheck.add("Wymaga dużego bagażnika")
            }
        }
        currentForm = ArrayList()
            Log.d("Formularz", "nowy")
        val answerLiveData = interfaceVM.getForm()
        answerLiveData.observe(viewLifecycleOwner){
            if(it != null){
                currentAnswer = it
                Log.d("interfaceVM.getForm()", currentAnswer.toString())

                if(currentAnswer!!.isEmpty()){
                    currentAnswer = ArrayList(formOpen.map { ans -> ans[1] })
                    if(formCheck.isNotEmpty()){
                        currentAnswer!!.add("nie")
                    }
                    interfaceVM.setForm(currentAnswer)
                }

                for ((index, i) in formOpen.withIndex()) {
                    val row =
                        requireActivity().layoutInflater.inflate(
                            R.layout.create_request_question_row,
                            null
                        )
                    row.questionRowText.text = i.first()
                    var adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, i.drop(1))
                        .also { adapter ->
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            row.questionRowSpinner.adapter = adapter
                        }
                    // previousAnswers?.let {  row.questionRowSpinner.setSelection(adapter.getPosition(it[index].toString())) }
                    if(i.drop(1).contains(currentAnswer!![index])){
                        row.questionRowSpinner.setSelection(adapter.getPosition(currentAnswer!![index]))
                    }else {
                        row.questionRowET.setText(currentAnswer!![index])
                        row.questionRowSpinner.setSelection(adapter.getPosition(i.last()))

                    }

                    // row.questionRowSpinner.setSelection(2)


                    row.questionRowSpinner.onItemSelectedListener = AnswerListener(this, index)
                    currentForm!!.add(row)
                    createRequestForm.addView(row)
                }
                for (i in formCheck) {
                    val row =
                        requireActivity().layoutInflater.inflate(
                            R.layout.create_request_checkbox_row,
                            null
                        )
                    row.checkBoxRow.text = i
                    row.checkBoxRow.isChecked = currentAnswer!!.last() != "nie"
                    row.checkBoxRow.setOnCheckedChangeListener { _, isChecked ->
                        if(isChecked){
                            currentAnswer!![currentAnswer!!.size - 1] = "tak"
                        } else {
                            currentAnswer!![currentAnswer!!.size - 1] = "nie"
                        }
                        interfaceVM.setForm(currentAnswer!!)
                    }
                    currentForm!!.add(row)
                    createRequestForm.addView(row)
                }

                answerLiveData.removeObservers(viewLifecycleOwner)
            }
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
        Log.d("onCategorySelected",position.toString())
        interfaceVM.setCategory(position)
        Log.d("First time", if(selectedCategory == null) "tak" else "nie")
        if(selectedCategory != null && selectedCategory != position){
            interfaceVM.setForm(arrayListOf())
        }
        selectedCategory = position
        setupRecycler(position)
        if (position == 0 && productsListCreator == null) {
            createRequestForm.visibility = View.VISIBLE
            productsListCreator =
                ProductsListCreator(createRequestForm, requireActivity(), productsVM)
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
            createRequestForm.visibility = View.GONE
        }
        if (position != 0 && position != 5) {
            createForm(position)
        }
        if(position == 5) {
            createRequestForm.visibility = View.GONE
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
        val description = getFormAnswers()+'\n'+if(createRequestDescription.text.toString().isNotBlank()) "Dodatkowy opis:\n"+createRequestDescription.text.toString() else ""
        val category = selectedCategory
        val location =
            GeoPoint(loc.latitude, loc.longitude)
        val longitudeZone = floor(loc.longitude).toInt()
        when {
            category == null -> showAlert(resources.getString(R.string.empty_request_category))
            description.isBlank() -> showAlert(resources.getString(R.string.empty_request_description))
            title.isBlank() -> showAlert(resources.getString(R.string.empty_request_title))
            else -> return Request(
                id = Calendar.getInstance().timeInMillis,
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
                            requestVM.insertRequestCloud(req)
                            val newChat = Chat(
                                id = req.id,
                                userInNeedId = user.id,
                                userInNeedName = user.name!!,
                            )
                            requestVM.insertRequestLocal(req)
                            chatVM.insertChat(newChat)
                            chatVM.createChatCloud(newChat)
                            userVM.decreaseToken()
                            interfaceVM.setTitle("")
                            interfaceVM.setDescription("")
                            interfaceVM.setCategory(null)
                            interfaceVM.setForm(null)
                            currentProductsList?.let { list ->
                                currentProductsList!!.forEach { it.listId = req.id }
                                requestVM.insertShoppingListForRequestLocal(list)
                                requestVM.insertShoppingListForRequestCloud(
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

    override fun onAnswerSelected(answer: String, row: Int, position: Int, isLast: Boolean) {
        if (isLast) {
            currentForm!![row].questionRowTIL.visibility = View.VISIBLE
            currentForm!![row].questionRowET.setOnFocusChangeListener { v, hasFocus ->
                if(!hasFocus){
                    currentAnswer!![row] = currentForm!![row].questionRowET.text.toString()
                    interfaceVM.setForm(currentAnswer!!)
                }
            }

        } else {
            currentForm!![row].questionRowTIL.visibility = View.GONE
            currentAnswer!![row] = answer
        }

        //currentForm!![currentForm!!.indexOf(row)] = row
         interfaceVM.setForm(currentAnswer!!)
        Log.d("Zmieniono",row.toString())
        Log.d("Zmieniona wartość", currentAnswer!![row])
        Log.d("getFormAnswers()",getFormAnswers())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        createRequestForm.removeAllViewsInLayout()
    }
}