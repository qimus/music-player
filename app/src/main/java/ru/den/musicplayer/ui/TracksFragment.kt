package ru.den.musicplayer.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_tracks.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

import ru.den.musicplayer.R
import ru.den.musicplayer.models.CurrentPlaylist
import ru.den.musicplayer.models.Track
import ru.den.musicplayer.searcher.MusicSearchCriteria
import ru.den.musicplayer.searcher.TrackSearcher
import ru.den.musicplayer.ui.adapters.TrackListAdapter
import ru.den.musicplayer.ui.viewmodel.TracksViewModel

private const val CRITERIA_PARAM = "criteria"
private const val PLAYLIST_PARAM = "playlist"

/**
 * Фрагмент отвечает за отображение списка треков
 */
class TracksFragment : Fragment(), TrackListAdapter.OnTrackListener {
    private val trackSearcher: TrackSearcher by inject()
    private val audioFilesAdapter = TrackListAdapter(mutableListOf(), this)
    private var searchCriteria: MusicSearchCriteria? = null
    private val viewModel: TracksViewModel by viewModel()
    private val currentPlaylist: CurrentPlaylist by inject()
    private lateinit var playlistName: String
    private var tracks = listOf<Track>()
    private lateinit var mediaPlayerHost: MediaPlayer

    private var mediaCallbacks = object : MediaPlayerCallbacks {
        override fun onStartPlay() {
            currentPlaylist.currentTrack?.let {
                audioFilesAdapter.setActiveTrackId(it.id)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mediaPlayerHost.registerMediaPlayerCallbacks(mediaCallbacks)

        val trackId = currentPlaylist.currentTrack?.id
        if (currentPlaylist.isPlaying && trackId != null) {
            audioFilesAdapter.setActiveTrackId(trackId)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayerHost.unregisterMediaPlayerCallbacks(mediaCallbacks)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            searchCriteria = it.getParcelable(CRITERIA_PARAM)
            playlistName = it.getString(PLAYLIST_PARAM)!!
        }

        configureObservable()
    }

    private fun configureObservable() {
        viewModel.getTracks().observe(this, Observer { tracks ->
            audioFilesAdapter.updateItems(tracks)
            this.tracks = tracks
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mediaPlayerHost = context as MediaPlayer
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tracks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tracks = trackSearcher.search(searchCriteria)
        audioFilesAdapter.updateItems(tracks)

        trackListRecyclerView.adapter = audioFilesAdapter
        trackListRecyclerView.layoutManager = LinearLayoutManager(context)

        viewModel.searchTracks(searchCriteria)
    }

    override fun onTrackSelected(trackIndex: Int) {
        if (currentPlaylist.playlistName != playlistName) {
            currentPlaylist.playlistName = playlistName
            currentPlaylist.setTracks(tracks)
        }

        currentPlaylist.currentTrackIndex = trackIndex
        mediaPlayerHost.play()
        currentPlaylist.currentTrack?.let {
            audioFilesAdapter.setActiveTrackId(it.id)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(criteria: MusicSearchCriteria?, playlist: String) =
            TracksFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(CRITERIA_PARAM, criteria)
                    putString(PLAYLIST_PARAM, playlist)
                }
            }
    }
}
