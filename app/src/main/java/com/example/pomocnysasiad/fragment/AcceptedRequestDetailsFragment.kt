package com.example.pomocnysasiad.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.pomocnysasiad.R
import com.example.pomocnysasiad.model.Product
import com.example.pomocnysasiad.model.Request
import com.example.pomocnysasiad.view.OnChatInteraction
import com.example.pomocnysasiad.viewmodel.RequestViewModel
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_accepted_request_details.*
import kotlinx.android.synthetic.main.fragment_request_details.*
import kotlinx.android.synthetic.main.list_readonly_product_row.view.*


class AcceptedRequestDetailsFragment : Fragment() {
    private val requestVM by viewModels<RequestViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_accepted_request_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val requestJson = arguments?.getString("request")
        val request: Request = Gson().fromJson(requestJson, Request::class.java)
        Log.d("AcceptedRequestDetailsFragment", request.toString())
        Log.d("AcceptedRequestDetailsFragment", requestJson)
        acceptedDetailsTitle.text = "tytul: ${request.title}"
        acceptedDetailsAuth.text = "Osoba potrzebujaca: ${request.userInNeedName}"
        acceptedDetailsDesc.text = "Opis: ${request.description}"

        if (request.category == 0) {
            val shoppingListLiveData = requestVM.getShoppingListLocal(request.id)
            shoppingListLiveData.observe(viewLifecycleOwner) {
                drawReadOnlyListOfProducts(it.list)
            }
        }
    }

    private fun drawReadOnlyListOfProducts(list: List<Product>) {
        acceptedDetailsShoppingList.removeAllViewsInLayout()
        acceptedDetailsShoppingList.descendantFocusability =
            ViewGroup.FOCUS_AFTER_DESCENDANTS
        for (i in list) {
            val row = layoutInflater.inflate(R.layout.list_readonly_product_row, null)
            row.listReadOnlyProductName.text = i.name

            if (i.amount == 0.0) {
                row.listReadOnlyProductAmount.text = ""
            } else {
                row.listReadOnlyProductAmount.text = i.amount.toString()
            }

            row.listReadOnlyProductUnit.text = i.unit
            row.listReadOnlyCheck.isChecked = i.isChecked
            row.listReadOnlyCheck.setOnCheckedChangeListener { _, isChecked ->
                row.listReadOnlyCheck.isChecked = isChecked
                i.isChecked = isChecked
                if (i.isChecked){
                    row.apply { setBackgroundResource(R.drawable.strikethrough_shape) }
                } else {
                    row.apply { setBackgroundResource(0) }
                }

            }
            acceptedDetailsShoppingList.addView(row)
        }

    }

}