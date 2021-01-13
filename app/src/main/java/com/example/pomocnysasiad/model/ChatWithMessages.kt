package com.example.pomocnysasiad.model

import androidx.room.Embedded
import androidx.room.Relation

data class ChatWithMessages(
    @Embedded val chat: Chat = Chat(),
    @Relation(
        parentColumn = "id",
        entityColumn = "chatId",
        entity = Message::class
    )
    var messages: List<Message> = emptyList()
)
