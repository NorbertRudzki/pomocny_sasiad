package com.example.pomocnysasiad.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pomocnysasiad.R
import com.example.pomocnysasiad.model.Category
import com.example.pomocnysasiad.model.Chat
import com.example.pomocnysasiad.model.ChatRequestRecord
import com.example.pomocnysasiad.model.Request
import com.example.pomocnysasiad.view.ChatRequestAdapter
import com.example.pomocnysasiad.view.OnChatInteraction
import com.example.pomocnysasiad.viewmodel.ChatViewModel
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_accepted_requests.*
import kotlinx.android.synthetic.main.fragment_my_requests.*


class AcceptedRequestsFragment : Fragment(), OnChatInteraction {
    private val chatVM by viewModels<ChatViewModel>()
    private var allRequests: List<Request>? = null
    private var allChats: List<Chat>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_accepted_requests, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        acceptedRequestsRecycler.layoutManager = LinearLayoutManager(requireContext())
        chatVM.getAllAcceptedRequests().observe(viewLifecycleOwner) { requests ->
            if (!requests.isNullOrEmpty()) {
                allRequests = requests
                chatVM.getAllVolunteerChats().observe(viewLifecycleOwner) { chats ->
                    if (!chats.isNullOrEmpty() && !allRequests.isNullOrEmpty()) {
                        allChats = chats
                        acceptedRequestsRecycler.adapter =
                            ChatRequestAdapter(prepareList(), this, requireContext())
                    }
                }
            }
        }


    }

    private fun prepareList(): List<ChatRequestRecord> {
        val list = ArrayList<ChatRequestRecord>()
        Log.d("requests size", allRequests!!.size.toString())
        Log.d("chats size", allChats!!.size.toString())
        for ((index, value) in allRequests!!.withIndex()) {
            Log.d("All Requests read", value.toString())
            list.add(
                ChatRequestRecord(
                    value.id,
                    allChats!![index].userInNeedName,
                    value.title,
                    Category.categoryList[value.category]["icon"] as Int
                )
            )
        }
        return list.toList()
    }

    override fun onDetailsClick(id: Long) {
        Log.d("onDetailsClick", id.toString())
        val req = allRequests!!.find { it.id == id }
        findNavController().navigate(
            AcceptedRequestsFragmentDirections.actionAcceptedRequestsFragment2ToAcceptedRequestDetailsFragment().actionId,
            bundleOf(
                "request" to Gson().toJson(req),
            )
        )
    }

    override fun onChatClick(id: Long) {
        findNavController().navigate(
            AcceptedRequestsFragmentDirections.actionAcceptedRequestsFragment2ToChatFragment2().actionId,
            bundleOf("id" to id)
        )
    }

}