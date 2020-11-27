package com.example.pomocnysasiad.viewmodel

import androidx.lifecycle.ViewModel
import com.example.pomocnysasiad.model.FirebaseRepository
import com.example.pomocnysasiad.model.Request

class RequestViewModel: ViewModel() {
    private val repository = FirebaseRepository()

    fun insertRequest(request: Request){
        repository.insertRequest(request)
    }
}