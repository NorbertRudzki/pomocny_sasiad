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
import com.example.pomocnysasiad.viewmodel.RequestViewModel
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_my_requests.*


class MyRequestsFragment : Fragment(), OnChatInteraction {
    private val chatVM by viewModels<ChatViewModel>()
    private var allRequests: List<Request>? = null
    private var allChats: List<Chat>? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_requests, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        myRequestsRecycler.layoutManager = LinearLayoutManager(requireContext())
        chatVM.getAllMyRequests().observe(viewLifecycleOwner) { requests ->
            if (!requests.isNullOrEmpty()) {
                allRequests = requests
                chatVM.getAllInNeedChats().observe(viewLifecycleOwner) { chats ->
                    if (!chats.isNullOrEmpty() && !allRequests.isNullOrEmpty()) {
                        allChats = chats
                        myRequestsRecycler.adapter =
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
            list.add(
                ChatRequestRecord(
                    value.id,
                    allChats!![index].volunteerName,
                    value.title,
                    Category.categoryList[value.category]["icon"] as Int
                )
            )
        }
        return list.toList()
    }

    override fun onDetailsClick(id: Long) {
        Log.d("detailsClicked", id.toString())
        findNavController().navigate(
            MyRequestsFragmentDirections.actionMyRequestsFragmentToAcceptedRequestDetailsFragment2().actionId,
            bundleOf(
                "request" to Gson().toJson(allRequests!!.find { it.id == id }),
            )
        )

    }

    override fun onChatClick(id: Long) {
        findNavController().navigate(MyRequestsFragmentDirections.actionMyRequestsFragmentToChatFragment().actionId,
        bundleOf("id" to id))
    }

}