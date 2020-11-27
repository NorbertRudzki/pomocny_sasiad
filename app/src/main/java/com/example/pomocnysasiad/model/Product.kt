package com.example.pomocnysasiad.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Product(
    @PrimaryKey val id: Long = Calendar.getInstance().timeInMillis,
    var listId: Long = 0,
    var name: String ="",
    var amount: Double =1.0,
    var unit: String ="",
    @field:JvmField
    var isChecked: Boolean = false
)