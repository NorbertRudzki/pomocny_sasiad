package com.example.pomocnysasiad.view

import android.view.View
import android.widget.AdapterView
import com.example.pomocnysasiad.model.Product

class ProductUnitAdapter(private  val unitSelector: UnitSelector, private val product: Product?): AdapterView.OnItemSelectedListener {
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (parent != null) {
                unitSelector.onUnitSelected(parent.getItemAtPosition(position).toString(), product)
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}
}

interface UnitSelector{
    fun onUnitSelected(unit: String, product: Product?)
}