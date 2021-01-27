package com.example.pomocnysasiad.view

import android.view.View

interface OnAnswerSelected {
    fun onAnswerSelected(answer: String, row: Int, position: Int, isLast: Boolean)
}