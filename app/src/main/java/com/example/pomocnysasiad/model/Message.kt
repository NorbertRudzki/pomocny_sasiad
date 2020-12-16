package com.example.pomocnysasiad.model

import androidx.room.Entity
import java.util.*

@Entity
data class Message(
    val id: Long = Calendar.getInstance().timeInMillis,
    val chatId: Long = 0L,
    val userId: String ="",
    val content: String =""
)
