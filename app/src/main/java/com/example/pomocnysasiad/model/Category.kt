package com.example.pomocnysasiad.model

import com.example.pomocnysasiad.R

object Category {
    //todo dać ikonke pinezki
    val categoryList = listOf(
        hashMapOf(
            "name" to "Zakupy",
            "image" to R.drawable.ic_shopping_cart_icon,
            "icon" to R.drawable.pin_shop
        ),
        hashMapOf(
            "name" to "Opieka nad\nzwierzętami",
            "image" to R.drawable.ic_pet_icon,
            "icon" to R.drawable.pin_pet
        ),
        hashMapOf(
            "name" to "Wyniesienie\nśmieci",
            "image" to R.drawable.ic_garbage_icon,
            "icon" to R.drawable.pin_trash
        ),
        hashMapOf(
            "name" to "Rozmowa",
            "image" to R.drawable.ic_phone_icon,
            "icon" to R.drawable.pin_phone
        ),
        hashMapOf(
            "name" to "Transport",
            "image" to R.drawable.ic_car_icon,
            "icon" to R.drawable.pin_transport
        ),
        hashMapOf(
            "name" to "Inne",
            "image" to R.drawable.ic_other_icon,
            "icon" to R.drawable.pin_else
        ),
    ) as List<HashMap<String, Any>>
}