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
import com.example.pomocnysasiad.model.Request
import com.example.pomocnysasiad.viewmodel.ChatViewModel
import com.example.pomocnysasiad.viewmodel.RequestViewModel
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_request_details.*


class RequestDetails : Fragment() {
    private val chatVM by viewModels<ChatViewModel>()
    private val requestVM by viewModels<RequestViewModel>()
    lateinit var request: Request
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
        return inflater.inflate(R.layout.fragment_request_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val requestJson = arguments?.getString("request")
        request = Gson().fromJson(requestJson, Request::class.java)

        Log.d("details", request.toString())
        detailsTitle.text = "${request.title}"
        detailsAuth.text = "${request.userInNeedName}"
        detailsDesc.text = "${request.description}"

        detailsOfferToHelp.setOnClickListener {

            val requestCounter = requestVM.getCountOfVolunteerRequests()
            requestCounter.observe(viewLifecycleOwner) { counter ->
                Log.d("requestCounter", counter.toString())
                if (counter < 10) {
                    val chatLiveData = chatVM.acceptRequestAndGetChat(request)
                    chatLiveData.observe(viewLifecycleOwner) {
                        if (it != null) {
                            chatVM.insertChat(it)
                            requestVM.insertRequestLocal(request)
                            if (request.category == 0) {
                                val shoppingListLiveData = requestVM.getShoppingList(request.id)
                                shoppingListLiveData.observe(viewLifecycleOwner) { list ->
                                    if (!list.isNullOrEmpty()) {
                                        requestVM.insertShoppingListForRequestLocal(list)
                                        requestVM.deleteShoppingListForRequestCloud(request.id)
                                        findNavController().navigate(RequestDetailsDirections.actionRequestDetailsToAcceptedRequestsFragment2())
                                    }
                                }
                            } else {
                                findNavController().navigate(RequestDetailsDirections.actionRequestDetailsToAcceptedRequestsFragment2())
                            }
                        }
                    }
                }
                requestCounter.removeObservers(viewLifecycleOwner)
            }
        }
        goToInNeedProfileBtn.setOnClickListener {
            findNavController().navigate(
                RequestDetailsDirections.actionRequestDetailsToAccountReputationFragment().actionId,
                bundleOf("userId" to request.userInNeedId)
            )
        }
    }
}