package ru.den.musicplayer.models

interface SoundEntity {
    val id: String
}

data class Album(
    override val id: String,
    var name: String,
    var key: String,
    var imageUrl: String? = null
) : SoundEntity
