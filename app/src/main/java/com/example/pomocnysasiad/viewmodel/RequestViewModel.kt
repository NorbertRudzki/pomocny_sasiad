package com.example.pomocnysasiad.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.pomocnysasiad.model.*

class RequestViewModel(application: Application) : AndroidViewModel(application) {
    private val firebaseRepository = FirebaseRepository()
    private val localRepository = LocalRepository(application.applicationContext)
    private val filterRequest: MutableLiveData<Filter> by lazy {
        MutableLiveData()
    }

    fun setFilter(filter: Filter) {
        filterRequest.postValue(filter)
    }


    fun insertRequestCloud(request: Request) {
        firebaseRepository.insertRequest(request)
    }

    fun insertShoppingListForRequestCloud(request: Request, list: ProductsListWrapper) {
        firebaseRepository.insertShoppingListForRequest(request, list)
    }

    fun getNearbyRequests() = filterRequest.switchMap { filter ->
        firebaseRepository.getNearbyRequests(filter)
    }

    fun insertRequestLocal(request: Request) {
        localRepository.insertRequest(request)
    }

    fun insertShoppingListForRequestLocal(products: List<Product>) {
        localRepository.insertManyProducts(products)
    }

    fun deleteLocalRequest(request: Request) {
        localRepository.deleteRequest(request)
    }

    fun deleteProductsByListId(id: Long) {
        localRepository.deleteProductsByListId(id)
    }

    fun getAllAcceptedRequests() =
        localRepository.getAllAcceptedRequest(firebaseRepository.getUserId())

    fun getAllMyRequests() =
        localRepository.getAllMyRequest(firebaseRepository.getUserId())

    fun getShoppingList(id: Long): LiveData<List<Product>> = firebaseRepository.getShoppingList(id)


}