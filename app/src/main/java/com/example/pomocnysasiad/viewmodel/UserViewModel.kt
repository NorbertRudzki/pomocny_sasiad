package com.example.pomocnysasiad.viewmodel

import androidx.lifecycle.ViewModel
import com.example.pomocnysasiad.model.Code
import com.example.pomocnysasiad.model.FirebaseRepository

class UserViewModel: ViewModel() {
    private val repository = FirebaseRepository()

    val user = repository.getUser()

    fun getUserId() = repository.getUserId()

    fun createUser(){
        repository.createNewUser()
    }

    fun increaseToken(){
        repository.increaseUsersToken()
    }

    fun increaseToken(x: Long){
        repository.increaseUsersToken(x)
    }

    fun decreaseToken(){
        repository.decreaseUsersToken()
    }

    fun decreaseToken(x: Long){
        repository.decreaseUsersToken(x)
    }

    fun isLogoutUserOrNotVerified() = repository.isLogoutUserOrNotVerified()

    fun isUserVerified() = repository.isUserVerified()

    fun sendEmailVerif(){
        repository.sendEmailVerif()
    }

    fun getUserName() = repository.getUserName()

    fun createCode(code: Code) = repository.createCode(code)

    fun getCode(codeID: Int) = repository.getCode(codeID)
    fun getCode(userID: String) = repository.getCode(userID)

    fun deleteCode(codeID: Int) = repository.deleteCode(codeID)
    fun deleteCode(userID: String) = repository.deleteCode(userID)
}