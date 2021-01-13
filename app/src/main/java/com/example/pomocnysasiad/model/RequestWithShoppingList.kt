package com.example.pomocnysasiad.model

import androidx.room.Embedded
import androidx.room.Relation

data class RequestWithShoppingList(
    @Embedded val request: Request,
    @Relation(
        parentColumn = "id",
        entityColumn = "listId",
        entity = Product::class
    )
    var list: List<Product>
)
