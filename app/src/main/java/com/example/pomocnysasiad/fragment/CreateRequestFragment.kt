package com.example.pomocnysasiad.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.example.pomocnysasiad.R
import com.example.pomocnysasiad.model.Request
import com.example.pomocnysasiad.view.CategoryAdapter
import com.example.pomocnysasiad.view.OnCategorySelected
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.fragment_create_request.*
import kotlinx.coroutines.runBlocking


class CreateRequestFragment : Fragment(), OnCategorySelected {
    private val firebaseFirestore = FirebaseFirestore.getInstance()
    private var selectedCategory: String? = null
    private val categoryList = listOf(
        hashMapOf(
            "name" to "zakupy",
            "image" to R.drawable.shopping
        ),
        hashMapOf(
            "name" to "zakupy2",
            "image" to R.drawable.shopping
        ),
        hashMapOf(
            "name" to "zakupy3",
            "image" to R.drawable.shopping
        ),
        hashMapOf(
            "name" to "zakupy4",
            "image" to R.drawable.shopping
        ),
        hashMapOf(
            "name" to "zakupy5",
            "image" to R.drawable.shopping
        ),
    ) as List<HashMap<String, Any>>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_request, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createRequestCategoryRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        createRequestCategoryRecycler.setItemViewCacheSize(categoryList.size)
        setupRecycler(null)

        createRequestCreateBT.setOnClickListener {

            firebaseFirestore.collection("users")
                .whereEqualTo("id", FirebaseAuth.getInstance().uid!!).limit(1).get().addOnSuccessListener { querySnapshot ->
                    if(!querySnapshot.isEmpty) {
                        val userData = querySnapshot.documents[0].data
                        userData?.let {
                            val tokens = it["tokens"] as Long
                            if(tokens > 0) {
                               val request = createRequest()
                                if(request != null) {
                                    insertNewRequest(request)
                                    decreaseUsersToken()
                                }
                            }
                        }
                    }
                }
        }

    }

    override fun onCategorySelected(position: Int) {
        selectedCategory = categoryList[position]["name"] as String
        setupRecycler(position)
    }

    private fun setupRecycler(selectedCategory: Int?) {
        createRequestCategoryRecycler.adapter =
            CategoryAdapter(requireContext(), categoryList, this, selectedCategory)
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

    private fun insertNewRequest(request: Request) {
        firebaseFirestore.collection("requestsForHelp").add(request)
    }

    private fun createRequest(): Request? {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val username = FirebaseAuth.getInstance().currentUser!!.displayName
        val title = createRequestName.text.toString()
        val description = createRequestDescription.text.toString()
        val category = selectedCategory
        val location = GeoPoint(52.209, 19.683)
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
                location = location
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

    private fun decreaseUsersToken(){
        firebaseFirestore.collection("users")
            .whereEqualTo("id", FirebaseAuth.getInstance().uid!!).limit(1).get().addOnSuccessListener {
              it.documents[0].reference.update("tokens", FieldValue.increment(-1))
            }
    }
}