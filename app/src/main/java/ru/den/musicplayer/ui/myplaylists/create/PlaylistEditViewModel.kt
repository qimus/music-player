package ru.den.musicplayer.ui.myplaylists.create

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.den.musicplayer.database.dao.PlaylistDao
import ru.den.musicplayer.database.dao.PlaylistItemDao

class PlaylistEditViewModel(
    private val playlistDao: PlaylistDao,
    private val playlistItemDao: PlaylistItemDao
) : ViewModel() {
    val name = MutableLiveData<String>()
}