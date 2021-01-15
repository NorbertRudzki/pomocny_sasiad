package com.example.pomocnysasiad.model

import androidx.room.*

@Dao
interface MessageDao {
    @Insert
    fun insertMessage(message: Message)

    @Query("DELETE FROM Message WHERE chatId = :id")
    fun deleteMessagesByChatId(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessages(messages: List<Message>)

}