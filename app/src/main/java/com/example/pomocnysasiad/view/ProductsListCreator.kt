package com.example.pomocnysasiad.view

import android.app.ActionBar
import android.app.Activity
import android.graphics.Typeface
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.marginTop
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
    private lateinit var addNewProductRow: View
    private var isNewProductAdded = false
init {
    val param = parent.layoutParams as ViewGroup.MarginLayoutParams
    param.setMargins(0,10,0,25)
    parent.layoutParams = param
}
    fun drawListOfProducts(list: List<Product>) {
        parent.removeAllViewsInLayout()
        parent.descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
        val label = TextView(activity)
        label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14F)
        label.setTextColor(ContextCompat.getColor(parent.context, R.color.primary))
        label.typeface = ResourcesCompat.getFont(parent.context,R.font.montserrat_regular)
        val lp = parent.layoutParams as ViewGroup.MarginLayoutParams
        lp.setMargins(10)
        label.layoutParams = lp
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
        val labelAddProduct = TextView(activity)
        labelAddProduct.text = activity.resources.getString(R.string.add_product)
        labelAddProduct.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14F)
        labelAddProduct.setTextColor(ContextCompat.getColor(parent.context, R.color.primary))
        labelAddProduct.typeface = ResourcesCompat.getFont(parent.context,R.font.montserrat_regular)
        labelAddProduct.layoutParams = lp
        parent.addView(labelAddProduct)

        addNewProductRow = activity.layoutInflater.inflate(R.layout.product_row, null)
        addNewProductRow.productBT.setImageResource(R.drawable.add)
        addNewProductRow.productAmount.setText("1")
        prepareUnitSelectorAdapter(addNewProductRow)
        addNewProductRow.productUnit.onItemSelectedListener =
            ProductUnitAdapter(this, null)
        addNewProductRow.productUnit.setSelection(0)

        addNewProductRow.productBT.setOnClickListener {
            isNewProductAdded = true
            if (addNewProductRow.productName.text.toString().isNotBlank()) {
                val amount = if (addNewProductRow.productAmount.text.toString().isNotBlank())
                    addNewProductRow.productAmount.text.toString().toDouble() else 0.0
                productsVM.addProduct(
                    Product(
                        name = addNewProductRow.productName.text.toString(),
                        amount = amount,
                        unit = selectedNewProductUnit
                    )
                )
            }
        }
        parent.addView(addNewProductRow)
        if (isNewProductAdded) {
            addNewProductRow.requestFocus()
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