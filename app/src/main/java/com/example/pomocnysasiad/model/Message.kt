package com.example.pomocnysasiad.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Message(
    @PrimaryKey val id: Long = 0L,
    val chatId: Long = 0L,
    val userId: String ="",
    val content: String =""
)
