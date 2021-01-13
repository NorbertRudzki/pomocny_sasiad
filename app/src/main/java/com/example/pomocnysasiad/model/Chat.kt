package com.example.pomocnysasiad.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Chat(
    @PrimaryKey val id:Long = 0L,
    var userInNeedId: String = "",
    var userInNeedName: String = "",
    var volunteerId: String = "",
    var volunteerName: String = "",
    var status: Int = 0
)
