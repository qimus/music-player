package ru.den.musicplayer.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.android.synthetic.main.fragment_player.next
import kotlinx.android.synthetic.main.fragment_player.prev
import kotlinx.android.synthetic.main.fragment_player.progressBar
import kotlinx.android.synthetic.main.fragment_player.trackTitle
import org.koin.android.ext.android.inject

import ru.den.musicplayer.R
import ru.den.musicplayer.models.PlaylistManager
import ru.den.musicplayer.models.Track
import ru.den.musicplayer.models.playlist.Playlist

/**
 * A simple [Fragment] subclass.
 * Use the [DetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DetailFragment : Fragment() {
    companion object {
        private const val TAG = "DetailFragment"

        @JvmStatic
        fun newInstance() =
            DetailFragment()
    }

    private val mediaCallbacks = object : MediaPlayerCallbacks {
        override fun onStartPlay() {
            updatePlayControls()
        }

        override fun onPause() {
            updatePlayControls()
        }

        override fun onPlaying(progress: Int) {
            updateProgressBar(progress)
        }

        override fun onStop() {

        }

        override fun onNextTrack() {

        }

        override fun onPrevTrack() {

        }
    }

    private val playlistManager: PlaylistManager by inject()
    private var playlist: Playlist = playlistManager.currentPlaylist
    private lateinit var mediaPlayer: MediaPlayer

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mediaPlayer = context as MediaPlayer
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_player, container, false)
    }

    private fun bindProgressBar() {
        progressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.progress?.let {
                    mediaPlayer.seekTo(it)
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()

        updatePlayControls()
        bindHandlers()
        bindProgressBar()
        updateProgressBar()
        mediaPlayer.registerMediaPlayerCallbacks(mediaCallbacks)
        Log.d(TAG, "onStart ${progressBar?.progress} : ${playlist.trackProgress} : ${playlist.currentTrack?.duration}")
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer.unregisterMediaPlayerCallbacks(mediaCallbacks)
    }

    private fun updatePlayControls() {
        if (playlistManager.isPlaying) {
            play?.setImageResource(R.drawable.ic_bottom_pause)
        } else {
            play?.setImageResource(R.drawable.ic_player_play)
        }

        playlist.currentTrack?.let {
            trackAlbum.text = it.album
            trackTitle.text = it.name
        }
    }

    private fun updateProgressBar(progress: Int, max: Int = playlist.currentTrack?.duration ?: 0) {
        progressBar?.max = max
        progressBar?.progress = progress

        playedTime?.text = Track.formatTrackTime(progress)
        estimateTime?.text = Track.formatTrackTime(max)
    }

    private fun updateProgressBar() {
        updateProgressBar(playlist.trackProgress)
    }

    private fun bindHandlers() {
        updatePlayControls()
        play.setOnClickListener {
            if (playlistManager.isPlaying) {
                mediaPlayer.pause()
            } else {
                mediaPlayer.play()
            }
        }

        next.setOnClickListener {
            mediaPlayer.nextTrack()
        }

        prev.setOnClickListener {
            mediaPlayer.prevTrack()
        }
    }
}
