package ru.den.musicplayer.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import ru.den.musicplayer.utils.Playlist

val appModule = module {
    single { Playlist(androidContext()) }
}
