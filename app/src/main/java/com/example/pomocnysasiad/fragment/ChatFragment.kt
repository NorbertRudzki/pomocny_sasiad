package com.example.pomocnysasiad.fragment

import android.app.AlertDialog
import android.graphics.Color
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pomocnysasiad.R
import com.example.pomocnysasiad.model.*
import com.example.pomocnysasiad.view.ChatAdapter
import com.example.pomocnysasiad.viewmodel.ChatViewModel
import com.example.pomocnysasiad.viewmodel.RequestViewModel
import com.example.pomocnysasiad.viewmodel.UserViewModel
import kotlinx.android.synthetic.main.fragment_chat.*
import java.util.*


class ChatFragment : Fragment() {
    private val chatVM by viewModels<ChatViewModel>()
    private val userVM by viewModels<UserViewModel>()
    private val requestVM by viewModels<RequestViewModel>()
    private var currentChat: ChatWithMessages? = null
    private var role: Int? = null //1 = volunteer   2 = in need
    private lateinit var preference: MyPreference
    private var id: Long = 0

    /*
    status
    0 - swieze need
    1 - vol zaproponowal
    2 - vol zaakceptowal 1+1
    3 - need zaakceptowal 1+2
    4 - oba zaakceptowane 1+1+2
    5 - ktos anulowal
    6 - vol potwierdzil 4+2
    7 - need potwierdzil 4+3
    9 - obaj potwierdzili 4+3+2
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onResume() {
        super.onResume()
        preference.setOpenChat(id)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        id = arguments?.getLong("id")!!
        preference = MyPreference(requireContext())

        chatRecycler.layoutManager = LinearLayoutManager(requireContext())
        role = preference.getRole()
        chatVM.getChatById(id).observe(viewLifecycleOwner) {
            if (it != null) {
                currentChat = it
                if (preference.getRole() == 1) {
                    chatUserName.text = currentChat!!.chat.userInNeedName
                    chatCheckAccount.setOnClickListener {
                        findNavController().navigate(
                            R.id.action_chatFragment2_to_accountReputationFragment,
                            bundleOf("userId" to currentChat!!.chat.userInNeedId)
                        )
                    }
                } else {
                    chatUserName.text = currentChat!!.chat.volunteerName
                    chatCheckAccount.setOnClickListener {
                        findNavController().navigate(
                            R.id.action_chatFragment_to_accountReputationFragment2,
                            bundleOf("userId" to currentChat!!.chat.volunteerId)
                        )
                    }
                }
                if (currentChat!!.chat.status == 5) {
                    informAboutRejected(currentChat!!)
                }
                chatRecycler.adapter = ChatAdapter(currentChat!!.messages, userVM.getUserId())
                chatRecycler.scrollToPosition(currentChat!!.messages.size - 1)
                prepareView()
            }
        }
        chatAccept.setOnClickListener {
            if (role == 1 && (currentChat!!.chat.status == 1 || currentChat!!.chat.status == 3)) {
                chatVM.setChatCloudStatus(id, currentChat!!.chat.status + 1)
                currentChat!!.chat.status += 1
            } else if (role == 2 && (currentChat!!.chat.status == 1 || currentChat!!.chat.status == 2)) {
                chatVM.setChatCloudStatus(id, currentChat!!.chat.status + 2)
                currentChat!!.chat.status += 2
            }
        }

        chatReject.setOnClickListener {
            rejectMakeSureDialog(currentChat!!)
        }

        chatFinish.setOnClickListener {
            if (role == 1 && (currentChat!!.chat.status == 4 || currentChat!!.chat.status == 7)) {
                chatVM.setChatCloudStatus(id, currentChat!!.chat.status + 2)
                currentChat!!.chat.status += 2
                findNavController().navigate(
                    ChatFragmentDirections.actionChatFragment2ToAssessFragment2().actionId,
                    bundleOf(
                        "userName" to currentChat!!.chat.userInNeedName,
                        "userId" to currentChat!!.chat.userInNeedId
                    )
                )
            } else if (role == 2 && (currentChat!!.chat.status == 4 || currentChat!!.chat.status == 6)) {
                chatVM.setChatCloudStatus(id, currentChat!!.chat.status + 3)
                currentChat!!.chat.status += 3
                findNavController().navigate(
                    ChatFragmentDirections.actionChatFragment2ToAssessFragment2().actionId,
                    bundleOf(
                        "userName" to currentChat!!.chat.volunteerName,
                        "userId" to currentChat!!.chat.volunteerId
                    )
                )
            }
        }

        chatSendMessage.setOnClickListener {
            val content = chatNewMessageChatET.text.toString()
            if (content.isNotBlank()) {
                chatVM.sendMessageCloud(
                    id,
                    Message(
                        Calendar.getInstance().timeInMillis,
                        id, userVM.getUserId(),
                        content
                    )
                )
                chatNewMessageChatET.setText("")
            }
        }
    }

    private fun prepareView() {
        when (currentChat!!.chat.status) {
            in 1..3 -> {
                chatAccept.visibility = View.VISIBLE
                chatReject.visibility = View.VISIBLE
                chatFinish.visibility = View.GONE
            }
            in 4..9 -> {
                chatAccept.visibility = View.GONE
                chatReject.visibility = View.GONE
                chatFinish.visibility = View.VISIBLE
            }
        }
        if ((role == 1 && currentChat!!.chat.status == 2) || (role == 2 && currentChat!!.chat.status == 3)) {
            chatAccept.setBackgroundColor(Color.GRAY)
            chatAccept.text = resources.getString(R.string.accepted)
        }
        if ((role == 1 && currentChat!!.chat.status == 6) || (role == 2 && currentChat!!.chat.status == 7)) {
            chatFinish.setBackgroundColor(Color.GRAY)
            chatFinish.text = resources.getString(R.string.finished)
        }
    }

    private fun rejectMakeSureDialog(chatWithMessages: ChatWithMessages) {
        AlertDialog.Builder(requireContext())
            .setMessage("Czy na pewno chcesz zrezygnować? Zgłoszenie powróci do listy oczekujących, może ktoś inny się nim zajmie")
            .setPositiveButton("Rezygnuje") { dialog, _ ->
                rejectAssessDialog(chatWithMessages)

            }
            .setNegativeButton("Powrót") { dialog, _ ->
                dialog.cancel()
            }.create().show()
    }

    private fun rejectAssessDialog(chatWithMessages: ChatWithMessages) {
        if (role == 2) {
            reCreateRequestCloud(chatWithMessages)
        }
        chatVM.deleteChatWithMessagesLocally(chatWithMessages)
        chatVM.setChatCloudStatus(chatWithMessages.chat.id, 5)
        AlertDialog.Builder(requireContext())
            .setMessage("Czy chcesz wystawić opinię użytkownikowi?")
            .setPositiveButton("Oceń") { dialog, _ ->
                val bundle =
                    if (role == 1) bundleOf(
                        "userId" to chatWithMessages.chat.userInNeedId,
                        "userName" to chatWithMessages.chat.userInNeedName
                    )
                    else bundleOf(
                        "userId" to chatWithMessages.chat.volunteerId,
                        "userName" to chatWithMessages.chat.volunteerName
                    )
                findNavController().navigate(
                    ChatFragmentDirections.actionChatFragment2ToAssessFragment2().actionId,
                    bundle
                )
            }
            .setNegativeButton("Wróć do listy") { dialog, _ ->
                findNavController().popBackStack()
            }.create().show()
    }

    private fun informAboutRejected(chatWithMessages: ChatWithMessages) {
        if (role == 2) {
            reCreateRequestCloud(chatWithMessages)
        } else {
            chatVM.deleteChatWithMessagesLocally(chatWithMessages)
        }
        chatVM.deleteChatCloud(chatWithMessages)


        AlertDialog.Builder(requireContext())
            .setMessage("Użytkownik zrezygnował z pomocy, czy chcesz wystawić mu opinię?")
            .setPositiveButton("Oceń") { dialog, _ ->
                val bundle =
                    if (role == 1) bundleOf(
                        "userId" to chatWithMessages.chat.userInNeedId,
                        "userName" to chatWithMessages.chat.userInNeedName
                    )
                    else bundleOf(
                        "userId" to chatWithMessages.chat.volunteerId,
                        "userName" to chatWithMessages.chat.volunteerName
                    )
                findNavController().navigate(
                    ChatFragmentDirections.actionChatFragment2ToAssessFragment2().actionId,
                    bundle
                )
            }
            .setNegativeButton("Wróć do listy") { dialog, _ ->
                findNavController().popBackStack()
            }.create().show()
    }

    private fun reCreateRequestCloud(chat: ChatWithMessages) {
        val requestLiveData = requestVM.getRequestWithShoppingListById(chat.chat.id)
        requestLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                var newRequest = it
                newRequest.request.id = Calendar.getInstance().timeInMillis
                requestVM.insertRequestCloud(newRequest.request)
                if (role == 2) {
                    requestVM.insertRequestLocal(newRequest.request)
                    val newChat = Chat(
                        id = newRequest.request.id,
                        userInNeedId = userVM.getUserId(),
                        userInNeedName = userVM.getUserName()!!,
                    )
                    chatVM.insertChat(newChat)
                    chatVM.createChatCloud(newChat)
                }
                if (it.list.isNotEmpty()) {
                    for (product in newRequest.list) {
                        product.listId = newRequest.request.id
                    }
                    requestVM.insertShoppingListForRequestCloud(
                        newRequest.request,
                        ProductsListWrapper(newRequest.list)
                    )
                    if (role == 2) {
                        requestVM.insertShoppingListForRequestLocal(newRequest.list)
                    }
                }
                chatVM.deleteChatWithMessagesLocally(chat)
                requestLiveData.removeObservers(viewLifecycleOwner)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("Chat", "onDestroyView")
        preference.setOpenChat(0L)
    }

}