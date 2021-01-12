package com.example.pomocnysasiad.model

import com.example.pomocnysasiad.R

object Category {
    //todo dać ikonke pinezki
    val categoryList = listOf(
        hashMapOf(
            "name" to "Zakupy\n",
            "image" to R.drawable.shopping,
            "icon" to R.drawable.pin_shop
        ),
        hashMapOf(
            "name" to "Opieka nad\nzwierzętami",
            "image" to R.drawable.shopping,
            "icon" to R.drawable.pin_pet
        ),
        hashMapOf(
            "name" to "Wyniesienie\nśmieci",
            "image" to R.drawable.shopping,
            "icon" to R.drawable.pin_trash
        ),
        hashMapOf(
            "name" to "Rozmowa\n",
            "image" to R.drawable.shopping,
            "icon" to R.drawable.pin_phone
        ),
        hashMapOf(
            "name" to "Transport\n",
            "image" to R.drawable.shopping,
            "icon" to R.drawable.pin_transport
        ),
        hashMapOf(
            "name" to "Inne\n",
            "image" to R.drawable.shopping,
            "icon" to R.drawable.pin_else
        ),
    ) as List<HashMap<String, Any>>
}