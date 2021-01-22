package com.example.pomocnysasiad.model

import com.example.pomocnysasiad.R

object Category {
    val categoryList = listOf(
        hashMapOf(
            "name" to "Zakupy",
            "image" to R.drawable.ic_shopping_cart_icon,
            "icon" to R.drawable.pin_shop,
            "filterIcon" to R.id.searchShopping
        ),
        hashMapOf(
            "name" to "Zwięrzeta",
            "image" to R.drawable.ic_pet_icon,
            "icon" to R.drawable.pin_pet,
            "filterIcon" to R.id.searchPet
        ),
        hashMapOf(
            "name" to "Śmieci",
            "image" to R.drawable.ic_garbage_icon,
            "icon" to R.drawable.pin_trash,
            "filterIcon" to R.id.searchGarbage

        ),
        hashMapOf(
            "name" to "Rozmowa",
            "image" to R.drawable.ic_phone_icon,
            "icon" to R.drawable.pin_phone,
            "filterIcon" to R.id.searchPhone
        ),
        hashMapOf(
            "name" to "Transport",
            "image" to R.drawable.ic_car_icon,
            "icon" to R.drawable.pin_transport,
            "filterIcon" to R.id.searchCar
        ),
        hashMapOf(
            "name" to "Inne",
            "image" to R.drawable.ic_other_icon,
            "icon" to R.drawable.pin_else,
            "filterIcon" to R.id.searchOther
        ),
    ) as List<HashMap<String, Any>>
}