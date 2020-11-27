package com.example.pomocnysasiad.model

data class User(
    val id: String ="",
    var name: String? = null,
    var tokens: Int = 0,
    var score: Int = 0
)