package ru.den.musicplayer.ui

import android.os.Bundle
import android.support.v4.media.session.MediaControllerCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_track_list.*

import ru.den.musicplayer.R
import ru.den.musicplayer.utils.Playlist

/**
 * A simple [Fragment] subclass.
 * Use the [TrackListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TrackListFragment : Fragment(), TrackListAdapter.OnTrackListener, MediaPlayerListener {

    interface Player {
        fun play(trackId: Int)
        fun pause(trackId: Int)
    }

    companion object {
        @JvmStatic
        fun newInstance() = TrackListFragment()
    }

    private val audioFilesAdapter = TrackListAdapter(mutableListOf(), this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_track_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context?.let {
            val files = Playlist.getAudioFilesFromDevice(it)
            audioFilesAdapter.updateItems(files)

            trackListRecyclerView.adapter = audioFilesAdapter
            trackListRecyclerView.layoutManager = LinearLayoutManager(context)
        }
    }

    override fun onPlayTrack(trackId: Int) {
        (activity as Player).play(trackId)
    }

    override fun onPauseTrack(trackId: Int) {
        (activity as Player).pause(trackId)
    }

    override fun play(trackId: Int) {
        audioFilesAdapter.setPlayingTrackIndex(trackId)
    }

    override fun pause(trackId: Int) {
        audioFilesAdapter.setPlayingTrackIndex(-1)
    }
}
