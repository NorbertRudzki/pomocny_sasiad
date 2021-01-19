package com.example.pomocnysasiad.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.firebase.firestore.GeoPoint
import java.util.*

@Entity
data class Request(
    @PrimaryKey var id: Long = 0L,
    val userInNeedId: String ="",
    val userInNeedName: String = "",
    val title: String = "",
    val description: String = "",
    val category: Int = 0,
    val location: GeoPoint = GeoPoint(0.0, 0.0),
    val longitudeZone: Int = 0
    )