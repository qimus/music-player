package ru.den.musicplayer.models

data class Artist(
    override val id: String,
    var name: String,
    var key: String
) : SoundEntity