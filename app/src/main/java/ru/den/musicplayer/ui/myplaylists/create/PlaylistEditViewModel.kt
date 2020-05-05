package ru.den.musicplayer.ui.myplaylists.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.den.musicplayer.database.dao.PlaylistDao
import ru.den.musicplayer.database.dao.PlaylistItemDao
import ru.den.musicplayer.models.Track
import ru.den.musicplayer.searcher.MusicSearchCriteria
import ru.den.musicplayer.searcher.TrackSearcher

class PlaylistEditViewModel(
    private val searcher: TrackSearcher,
    private val playlistDao: PlaylistDao,
    private val playlistItemDao: PlaylistItemDao
) : ViewModel() {
    val name = MutableLiveData<String>()

    private val _selectedTracks = MutableLiveData<List<Track>>()
    val selectedTracks: LiveData<List<Track>>
        get() = _selectedTracks

    fun searchTracks(criteria: MusicSearchCriteria?) {
        _selectedTracks.value = searcher.search(criteria)
    }
}
