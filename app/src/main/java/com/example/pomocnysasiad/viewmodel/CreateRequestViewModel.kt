package com.example.pomocnysasiad.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pomocnysasiad.model.Product

class CreateRequestViewModel : ViewModel() {
    init {
        Log.d("CreateRequestViewModel","init")
    }
    private val title: MutableLiveData<String> by lazy {
        MutableLiveData()
    }
    private val description: MutableLiveData<String> by lazy {
        MutableLiveData()
    }
    private val category: MutableLiveData<Int> by lazy {
        MutableLiveData()
    }
    private val shoppingList: MutableLiveData<List<Product>?> by lazy {
        MutableLiveData()
    }

    fun setTitle(title: String) {
        this.title.value = title
    }

    fun setDescription(description: String) {
        this.description.value = description
    }

    fun setCategory(category: Int) {
        this.category.value = category
    }

    fun setShoppingList(shoppingList: List<Product>?) {
        this.shoppingList.value = shoppingList
    }

    fun getTitle(): LiveData<String> = title

    fun getDescription(): LiveData<String> = description

    fun getCategory(): LiveData<Int> = category

    fun getShoppingList(): LiveData<List<Product>?> = shoppingList

}