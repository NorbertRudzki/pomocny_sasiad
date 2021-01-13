package com.example.pomocnysasiad.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MessageDao {
    @Insert
    fun insertMessage(message: Message)

    @Query("DELETE FROM Message WHERE chatId = :id")
    fun deleteMessagesByChatId(id: Long)

}