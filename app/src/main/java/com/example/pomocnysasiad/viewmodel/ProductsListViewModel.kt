package com.example.pomocnysasiad.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pomocnysasiad.model.Product

class ProductsListViewModel : ViewModel() {
    private val products = MutableLiveData<ArrayList<Product>>()

    init {
        products.value = ArrayList()
    }

    fun getProducts(): LiveData<ArrayList<Product>> = products

    fun addProduct(product: Product) {
        val list = products.value
        list?.add(product)
        products.value = list
    }

    fun addProducts(products: List<Product>){
        this.products.value = ArrayList(products)
    }

    fun removeProduct(product: Product) {
        val list = products.value
        list?.remove(product)
        products.value = list
    }

}