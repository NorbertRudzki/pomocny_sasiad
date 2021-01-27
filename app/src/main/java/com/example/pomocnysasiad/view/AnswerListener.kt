package com.example.pomocnysasiad.view

import android.util.Log
import android.view.View
import android.widget.AdapterView

class AnswerListener(private  val onAnswerSelected: OnAnswerSelected, private val row: Int): AdapterView.OnItemSelectedListener {
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (parent != null) {

            onAnswerSelected.onAnswerSelected(parent.getItemAtPosition(position).toString(), row, position, parent.count == position +1)
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}
}

