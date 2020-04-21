package ru.den.musicplayer.models

data class Year(
    var year: String
) : SoundEntity {
    override val id: String
        get() = year
}
