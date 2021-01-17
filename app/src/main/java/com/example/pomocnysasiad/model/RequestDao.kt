package com.example.pomocnysasiad.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RequestDao {
    @Insert
    fun insertRequest(request: Request)

    @Delete
    fun deleteRequest(request: Request)

    @Update
    fun updateRequest(request: Request)

    //volunteer
    @Query("SELECT * FROM Request WHERE userInNeedId NOT LIKE :uid")
    fun getAllAcceptedRequest(uid: String): LiveData<List<Request>>

    //inNeed
    @Query("SELECT * FROM Request WHERE userInNeedId LIKE :uid")
    fun getAllMyRequest(uid: String): LiveData<List<Request>>

    @Query("DELETE FROM Request WHERE id = :id")
    fun deleteRequestById(id: Long)

    @Transaction
    @Query("SELECT * FROM Request WHERE id = :uid")
    fun getRequestWithShoppingListById(uid: Long): LiveData<RequestWithShoppingList>
}