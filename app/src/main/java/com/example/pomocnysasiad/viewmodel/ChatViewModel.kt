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

    fun createChatCloud(chat: Chat){
        firebaseRepository.createChat(chat)
    }


    fun acceptRequestAndGetChat(request: Request):LiveData<Chat> = firebaseRepository.acceptRequestAndGetChat(request)

}