package ru.den.musicplayer.models

data class Album(
    var id: String,
    var name: String,
    var key: String,
    var imageUrl: String? = null
)
