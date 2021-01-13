package com.example.pomocnysasiad.viewmodel

import androidx.lifecycle.ViewModel
import com.example.pomocnysasiad.model.FirebaseRepository

class UserViewModel: ViewModel() {
    private val repository = FirebaseRepository()

    val user = repository.getUser()

    fun createUser(){
        repository.createNewUser()
    }

    fun increaseToken(){
        repository.increaseUsersToken()
    }

    fun decreaseToken(){
        repository.decreaseUsersToken()
    }
    fun isLogoutUserOrNotVerified() = repository.isLogoutUserOrNotVerified()

    fun isUserVerified() = repository.isUserVerified()

    fun sendEmailVerif(){
        repository.sendEmailVerif()
    }

    fun getUserName() = repository.getUserName()
}