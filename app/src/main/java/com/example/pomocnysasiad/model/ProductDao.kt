package com.example.pomocnysasiad.model

import androidx.room.*

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMany(products: List<Product>)

    @Update
    suspend fun update(product: Product)

    @Delete
    suspend fun delete(product: Product)

    @Delete
    suspend fun deleteMany(products: List<Product>)

    @Query("DELETE FROM Product WHERE listId LIKE :id")
    suspend fun deleteProductsByListId(id: Long)


}