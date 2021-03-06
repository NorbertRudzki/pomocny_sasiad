package com.example.pomocnysasiad.fragment

import android.content.Intent
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
import com.example.pomocnysasiad.service.InNeedRequestService
import com.example.pomocnysasiad.R
import com.example.pomocnysasiad.model.Category
import com.example.pomocnysasiad.model.Chat
import com.example.pomocnysasiad.model.ChatRequestRecord
import com.example.pomocnysasiad.model.Request
import com.example.pomocnysasiad.view.ChatRequestAdapter
import com.example.pomocnysasiad.view.OnChatInteraction
import com.example.pomocnysasiad.viewmodel.ChatViewModel
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

        if (!InNeedRequestService.isSearching) {
            val intentService = Intent(requireContext(), InNeedRequestService::class.java)
            requireContext().stopService(intentService)
            requireContext().startService(intentService)
        }


        val notificationId = arguments?.getLong("notification")

        myRequestsRecycler.layoutManager = LinearLayoutManager(requireContext())
        chatVM.getAllMyRequests().observe(viewLifecycleOwner) { requests ->
            if (!requests.isNullOrEmpty()) {
                Log.d("LOG","!requests.isNullOrEmpty()")
                emptyImage.visibility = View.GONE
                myRequestsRecycler.visibility = View.VISIBLE
                allRequests = requests
                chatVM.getAllInNeedChats().observe(viewLifecycleOwner) { chats ->
                    if (!chats.isNullOrEmpty() && !allRequests.isNullOrEmpty()) {
                        Log.d("LOG","!chats.isNullOrEmpty()")
                        emptyImage.visibility = View.GONE
                        myRequestsRecycler.visibility = View.VISIBLE
                        allChats = chats
                        myRequestsRecycler.adapter =
                            ChatRequestAdapter(prepareList(), this, requireContext())

                        if (notificationId != null && notificationId != 0L) {
                            arguments?.putLong("notification", 0L)
                            onChatClick(notificationId)
                        }
                    } else {
                        Log.d("LOG ELSE","!chats.isNullOrEmpty()")
                        emptyImage.visibility = View.VISIBLE
                        myRequestsRecycler.visibility = View.GONE
                    }
                }
            } else {
                Log.d("LOG ELSE","!requests.isNullOrEmpty()")
                emptyImage.visibility = View.VISIBLE
                myRequestsRecycler.visibility = View.GONE
            }
        }

    }

    private fun prepareList(): List<ChatRequestRecord> {
        val list = ArrayList<ChatRequestRecord>()
        for ((index, value) in allChats!!.withIndex()) {
            if(index < allRequests!!.size){
                list.add(
                    ChatRequestRecord(
                        value.id,
                        value.volunteerName,
                        allRequests!![index].title,
                        Category.categoryList[allRequests!![index].category]["icon"] as Int
                    )
                )
            }
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
        Log.d("onChatClick", id.toString())
        Log.d("(allChats!!.map { it.id }",allChats!!.map { it.id }.toString())
        if (allChats!!.map { it.id }.contains(id)) {
            findNavController().navigate(
                MyRequestsFragmentDirections.actionMyRequestsFragmentToChatFragment().actionId,
                bundleOf("id" to id)
            )
        }
    }

}