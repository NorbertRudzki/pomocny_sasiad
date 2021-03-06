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

    fun changeCategoryFilter(category: Int){
        val filter = filterRequest.value
        if(filter!!.categoryList.contains(category)){
            filter.categoryList = filter.categoryList.filter { it != category }
        } else {
            val newList = ArrayList<Int>(filter.categoryList)
            newList.add(category)
            filter.categoryList = newList.toList()
        }
        filterRequest.postValue(filter)
    }

    fun getFilter(): LiveData<Filter> = filterRequest

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

    fun deleteShoppingListForRequestCloud(id: Long){
        firebaseRepository.deleteShoppingListForRequest(id)
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

    fun getShoppingListLocal(id: Long) = localRepository.getRequestWithShoppingListById(id)

    fun getRequestWithShoppingListById(uid: Long) = localRepository.getRequestWithShoppingListById(uid)

    fun getCountOfVolunteerRequests() = localRepository.getCountOfVolunteerRequests(firebaseRepository.getUserId())

    fun getCountOfInNeedRequests() = localRepository.getCountOfInNeedRequests(firebaseRepository.getUserId())


}