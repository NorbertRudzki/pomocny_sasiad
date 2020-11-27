package com.example.pomocnysasiad.view

import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setMargins
import com.example.pomocnysasiad.R
import com.example.pomocnysasiad.model.Product
import com.example.pomocnysasiad.viewmodel.ProductsListViewModel
import kotlinx.android.synthetic.main.product_row.view.*

class ProductsListCreator(
    private val parent: LinearLayout,
    val activity: Activity,
    private val productsVM: ProductsListViewModel
) : UnitSelector {
    var selectedNewProductUnit = ""
    private lateinit var addNewproductRow: View
    private var isNewProductAdded = true
init {
    val param = parent.layoutParams as ViewGroup.MarginLayoutParams
    param.setMargins(0,10,0,25)
    parent.layoutParams = param
}
    fun drawListOfProducts(list: List<Product>) {
        parent.removeAllViewsInLayout()
        parent.descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
        val label = TextView(activity)
        label.text = activity.resources.getString(R.string.shopping_list)
        parent.addView(label)
        for (i in list) {
            val row = activity.layoutInflater.inflate(R.layout.product_row, null)
            row.productName.setText(i.name)
            row.productName.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    i.name = row.productName.text.toString()
                }
            }
            row.productAmount.setText(i.amount.toString())
            row.productAmount.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    if (row.productAmount.text.toString().isNotBlank()) {
                        i.amount = row.productAmount.text.toString().toDouble()
                    } else {
                        i.amount = 0.0
                    }
                }
            }
            val adapter = prepareUnitSelectorAdapter(row)
            row.productUnit.onItemSelectedListener = ProductUnitAdapter(this, i)
            row.productUnit.setSelection(adapter.getPosition(i.unit))

            row.productBT.setOnClickListener {
                row.visibility = View.GONE
                productsVM.removeProduct(i)
            }
            parent.addView(row)
        }
        addNewproductRow = activity.layoutInflater.inflate(R.layout.product_row, null)
        addNewproductRow.productBT.setImageResource(R.drawable.add)
        addNewproductRow.productAmount.setText("1")
        prepareUnitSelectorAdapter(addNewproductRow)
        addNewproductRow.productUnit.onItemSelectedListener =
            ProductUnitAdapter(this, null)
        addNewproductRow.productUnit.setSelection(0)

        addNewproductRow.productBT.setOnClickListener {
            isNewProductAdded = true
            if (addNewproductRow.productName.text.toString().isNotBlank()) {
                val amount = if (addNewproductRow.productAmount.text.toString().isNotBlank())
                    addNewproductRow.productAmount.text.toString().toDouble() else 0.0
                productsVM.addProduct(
                    Product(
                        name = addNewproductRow.productName.text.toString(),
                        amount = amount,
                        unit = selectedNewProductUnit
                    )
                )
            }
        }
        parent.addView(addNewproductRow)
        if (isNewProductAdded) {
            addNewproductRow.requestFocus()
            isNewProductAdded = false
        }
    }

    private fun prepareUnitSelectorAdapter(row: View) = ArrayAdapter.createFromResource(
        activity.applicationContext,
        R.array.units,
        R.layout.spinner_item
    ).also { adapter ->
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        row.productUnit.adapter = adapter
    }

    override fun onUnitSelected(unit: String, product: Product?) {
        if (product != null) {
            product.unit = unit
        }
        selectedNewProductUnit = unit
    }

}