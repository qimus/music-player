package ru.den.musicplayer.ui.myplaylists.chooseTracks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.den.musicplayer.models.Track
import ru.den.musicplayer.searcher.MusicSearchCriteria
import ru.den.musicplayer.searcher.TrackSearcher

class ChooseTracksViewModel(private val searcher: TrackSearcher) : ViewModel() {
    private var _tracks = MutableLiveData<List<Track>>()
    val tracks: LiveData<List<Track>>
        get() = _tracks

    val isSearch = MutableLiveData(false)
    val searchValue = MutableLiveData("")
    val selectedItems = mutableSetOf<String>()

    private val _eventOnSaveTracks = MutableLiveData<Boolean>()
    val eventOnSaveTracks: LiveData<Boolean>
        get() = _eventOnSaveTracks

    fun setIsSearchMode() {
        if (isSearch.value == false) {
            isSearch.value = true
            searchValue.value = ""
        }
    }

    fun searchTracks(criteria: MusicSearchCriteria?) {
        _tracks.value = searcher.search(criteria)
    }

    fun addTracks() {
        _eventOnSaveTracks.value = true
    }

    fun onSaveTracksFinished() {
        _eventOnSaveTracks.value = false
    }
}