package ru.den.musicplayer.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.den.musicplayer.models.Track
import ru.den.musicplayer.searcher.MusicSearchCriteria
import ru.den.musicplayer.searcher.TrackSearcher

class TracksViewModel(private val searcher: TrackSearcher) : ViewModel() {
    private val tracks = MutableLiveData<List<Track>>()

    fun getTracks(): LiveData<List<Track>> = tracks

    fun searchTracks(criteria: MusicSearchCriteria?) {
        tracks.value = searcher.search(criteria)
    }
}
