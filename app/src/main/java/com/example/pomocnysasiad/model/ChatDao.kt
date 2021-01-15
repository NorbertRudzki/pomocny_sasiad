package com.example.pomocnysasiad.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ChatDao {

    @Insert
    fun insertChat(chat: Chat)

    @Delete
    fun deleteChat(chat: Chat)

    @Update
    fun updateChat(chat: Chat)

    @Update
    fun updateChats(chats: List<Chat>)

    @Transaction
    @Query("SELECT * FROM Chat WHERE id = :id")
    fun getChatById(id: Long):LiveData<ChatWithMessages>

    @Query("SELECT * FROM Chat WHERE volunteerId LIKE :id")
    fun getAllVolunteerChats(id: String):LiveData<List<Chat>>

    @Query("SELECT * FROM Chat WHERE userInNeedId LIKE :id")
    fun getAllInNeedChats(id: String):LiveData<List<Chat>>
}