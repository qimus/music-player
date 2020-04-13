package ru.den.musicplayer.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import ru.den.musicplayer.models.PlaylistManager
import ru.den.musicplayer.models.playlist.AllTracksPlaylist
import ru.den.musicplayer.utils.Playlist

val appModule = module {
    single { Playlist(androidContext()) }
    single {
        val playlistManager = PlaylistManager()
        playlistManager.addPlaylist(AllTracksPlaylist(androidContext()))

        playlistManager
    }
}
