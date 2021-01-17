package com.example.pomocnysasiad.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.example.pomocnysasiad.model.*

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val firebaseRepository = FirebaseRepository()
    private val localRepository = LocalRepository(application.applicationContext)

    fun getAllAcceptedRequests() =
        localRepository.getAllAcceptedRequest(firebaseRepository.getUserId())

    fun getAllMyRequests() =
        localRepository.getAllMyRequest(firebaseRepository.getUserId())

    fun getAllVolunteerChats(): LiveData<List<Chat>> = getAllAcceptedRequests().switchMap {
        localRepository.getAllVolunteerChats(firebaseRepository.getUserId())
    }

    fun getAllInNeedChats(): LiveData<List<Chat>> = getAllMyRequests().switchMap {
        localRepository.getAllInNeedChats(firebaseRepository.getUserId())
    }

    fun getChatById(id: Long):LiveData<ChatWithMessages> = localRepository.getChatById(id)

    fun insertChat(chat: Chat){
        localRepository.insertChat(chat)
    }

    fun insertMessage(message: Message){
        localRepository.insertMessage(message)
    }

    fun sendMessageCloud(id: Long, message: Message){
        firebaseRepository.sendMessage(id, message)
    }

    fun createChatCloud(chat: Chat){
        firebaseRepository.createChat(chat)
    }

    fun setChatCloudStatus(id: Long, status: Int){
        firebaseRepository.setChatCloudStatus(id, status)
    }

    fun deleteChatWithMessagesLocally(chatWithMessages: ChatWithMessages){
        localRepository.deleteChat(chatWithMessages.chat)
        localRepository.deleteMessagesByChatId(chatWithMessages.chat.id)
        localRepository.deleteRequestById(chatWithMessages.chat.id)
        localRepository.deleteProductsByListId(chatWithMessages.chat.id)
    }

    fun deleteMessagesByChatId(id: Long){
        localRepository.deleteMessagesByChatId(id)
    }

    fun deleteChatCloud(chatWithMessages: ChatWithMessages){
        firebaseRepository.deleteChat(chatWithMessages.chat)
    }
    fun clearChatCloud(chat: Chat){
        firebaseRepository.clearChat(chat)
    }

    fun acceptRequestAndGetChat(request: Request):LiveData<Chat> = firebaseRepository.acceptRequestAndGetChat(request)

}