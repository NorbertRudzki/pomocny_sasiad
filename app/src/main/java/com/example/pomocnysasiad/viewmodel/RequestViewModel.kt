package com.example.pomocnysasiad.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.pomocnysasiad.model.*

class RequestViewModel : ViewModel() {
    private val repository = FirebaseRepository()
    private val filterRequest: MutableLiveData<Filter> by lazy {
        MutableLiveData()
    }

    fun setFilter(filter: Filter){
        filterRequest.postValue(filter)
    }


    fun insertRequest(request: Request) {
        repository.insertRequest(request)
    }

    fun insertShoppingListForRequest(request: Request, list: ProductsListWrapper) {
        repository.insertShoppingListForRequest(request, list)
    }

    fun getNearbyRequests() = filterRequest.switchMap { filter ->
        repository.getNearbyRequests(filter)
    }

}