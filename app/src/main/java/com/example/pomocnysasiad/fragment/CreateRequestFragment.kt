package com.example.pomocnysasiad.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.example.pomocnysasiad.R
import com.example.pomocnysasiad.model.*
import com.example.pomocnysasiad.view.CategoryAdapter
import com.example.pomocnysasiad.view.OnCategorySelected
import com.example.pomocnysasiad.view.ProductsListCreator
import com.example.pomocnysasiad.viewmodel.ProductsListViewModel
import com.example.pomocnysasiad.viewmodel.RequestViewModel
import com.example.pomocnysasiad.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.fragment_create_request.*
import kotlinx.coroutines.runBlocking


class CreateRequestFragment : Fragment(), OnCategorySelected {
    private val userVM by viewModels<UserViewModel>()
    private val requestVM by viewModels<RequestViewModel>()
    private val productsVM by viewModels<ProductsListViewModel>()
    private var currentUser: User? = null
    private var selectedCategory: String? = null
    private var productsListCreator: ProductsListCreator? = null
    private var currentProductsList : List<Product>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_request, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userVM.user.observe(viewLifecycleOwner) {
            if (it != null) {
                currentUser = it
            }
        }

        createRequestCategoryRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        createRequestCategoryRecycler.setItemViewCacheSize(Category.categoryList.size)
        setupRecycler(null)

        createRequestCreateBT.setOnClickListener {

            currentUser?.let { user ->
                if (user.tokens > 0) {
                    val request = createRequest()
                    request?.let { req ->
                        requestVM.insertRequest(req)
                        userVM.decreaseToken()
                        currentProductsList?.let { list ->
                            currentProductsList!!.forEach { it.listId = req.id}
                            requestVM.insertShoppingListForRequest(req, ProductsListWrapper(list))
                        }
                    }
                }
            }
        }

    }

    override fun onCategorySelected(position: Int) {
        selectedCategory = Category.categoryList[position]["name"] as String
        setupRecycler(position)
        if (Category.categoryList[position]["name"].toString() == "Zakupy" && productsListCreator == null) {
            createRequestShoppingList.visibility = View.VISIBLE
            productsListCreator = ProductsListCreator(createRequestShoppingList, requireActivity(), productsVM)
            productsListCreator!!.drawListOfProducts(emptyList())
            productsVM.getProducts().observe(viewLifecycleOwner){
                if(it != null) {
                    currentProductsList = it
                    productsListCreator!!.drawListOfProducts(it)
                }
            }
        } else if(productsListCreator != null) {
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

    private fun createRequest(): Request? {
        //todo upewnij sie, ze name zostalo ustawione w profilu
        val userId = currentUser!!.id
        val username = currentUser!!.name
        val title = createRequestName.text.toString()
        val description = createRequestDescription.text.toString()
        val category = selectedCategory
        val location = GeoPoint(50.0, 20.0)
        val longitudeZone = 20
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

    private fun showAlert(message: String) {
        AlertDialog.Builder(requireActivity())
            .setTitle(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
    }


}