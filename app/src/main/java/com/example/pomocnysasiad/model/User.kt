package com.example.pomocnysasiad.model

data class User(
    val id: String ="",
    var name: String? = null,
    var tokens: Int = 0,
    var score: Float = 0F,
    var helpCounter: Int = 0,
    var opinionList: List<Opinion> = emptyList()
)