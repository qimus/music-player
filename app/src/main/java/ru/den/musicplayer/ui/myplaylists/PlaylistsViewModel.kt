package ru.den.musicplayer.ui.myplaylists

import androidx.lifecycle.ViewModel
import ru.den.musicplayer.database.dao.PlaylistDao
import ru.den.musicplayer.database.models.Playlist

class PlaylistsViewModel(private val playlistDao: PlaylistDao) : ViewModel() {
    val playlists = playlistDao.getPlaylists()

    fun addNewPlaylist(playlist: Playlist) {
        playlistDao.insert(playlist)
    }
}