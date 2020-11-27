package com.example.pomocnysasiad.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.GeoPoint
import java.util.*

@Entity
data class Request(
    @PrimaryKey val id: Long = Calendar.getInstance().timeInMillis,
    val userInNeedId: String ="",
    val userInNeedName: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val location: GeoPoint = GeoPoint(0.0, 0.0),
    val longitudeZone: Int = 0
    )