package ru.den.musicplayer.ui

import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_track_list.*
import org.koin.android.ext.android.inject
import ru.den.musicplayer.R
import ru.den.musicplayer.models.Track
import ru.den.musicplayer.services.MediaPlayerService
import ru.den.musicplayer.utils.Playlist

/**
 * A simple [Fragment] subclass.
 * Use the [TrackListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TrackListFragment : Fragment(), TrackListAdapter.OnTrackListener,
    MediaPlayerCallbacks.Holder {

    companion object {
        private const val TAG = "TrackListFragment"

        @JvmStatic
        fun newInstance() = TrackListFragment()
        private var bottomPlayerIsVisible = false
    }

    private val playlist: Playlist by inject()
    private val audioFilesAdapter = TrackListAdapter(mutableListOf(), this)
    private lateinit var mediaPlayer: MediaPlayer

    private var mediaCallbacks = object : MediaPlayerCallbacks {
        override fun onStartPlay() {
            updateMiniPlayerAction()
        }

        override fun onPause() {
            updateMiniPlayerAction()
        }

        override fun onPlaying(pos: Int) {
            playlist.currentTrack?.let {
                progressBar?.max = it.duration ?: 0
                progressBar?.progress = pos

                if (elapsedTime != null) {
                    elapsedTime.text =
                        "${Track.formatTrackTime(progressBar.progress)}/${Track.formatTrackTime(progressBar.max)}"
                }
            }
        }

        override fun onStop() {
            updateMiniPlayerAction()
        }

        override fun onNextTrack() {

        }

        override fun onPrevTrack() {

        }
    }

    override fun onStart() {
        super.onStart()
        context?.let {
            MediaPlayerService.startService(it)
        }
        configureBottomMediaPlayer()
        if (bottomPlayerIsVisible) {
            showBottomMediaPlayerControl()
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mediaPlayer = context as MediaPlayer
    }

    private fun updateMiniPlayerAction() {
        if (playlist.isPlaying) {
            musicAction?.setImageResource(R.drawable.ic_bottom_pause)
            audioFilesAdapter.setActiveTrackIndex(playlist.trackIndex)
        } else {
            musicAction?.setImageResource(R.drawable.ic_bottom_play)
            audioFilesAdapter.setActiveTrackIndex(-1)
        }
        trackTitle?.text = playlist.currentTrack?.name
    }

    private fun configureBottomMediaPlayer() {
        musicAction.setOnClickListener {
            if (playlist.isPlaying) {
                pause()
            } else {
                play()
            }
        }

        next.setOnClickListener {
            mediaPlayer.nextTrack()
        }

        prev.setOnClickListener {
            mediaPlayer.prevTrack()
        }

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

    private fun showBottomMediaPlayerControl() {
        val animator = ValueAnimator.ofInt(miniPlayer.layoutParams.height, 250).apply {
            duration = 300
            interpolator = LinearInterpolator()
            start()
        }

        animator.addUpdateListener {
            val value = it.animatedValue as Int
            miniPlayer.layoutParams.height = value
            miniPlayer.requestLayout()
        }

        bottomPlayerIsVisible = true
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
            val files = playlist.tracks
            audioFilesAdapter.updateItems(files)

            trackListRecyclerView.adapter = audioFilesAdapter
            trackListRecyclerView.layoutManager = LinearLayoutManager(context)
        }

        miniPlayer.setOnClickListener {
            activity?.supportFragmentManager?.run {
                val fragment = DetailFragment.newInstance()
                beginTransaction().replace(R.id.fragmentContainer, fragment).addToBackStack(null)
                    .commit()
            }
        }
    }

    override fun onTrackSelected(trackId: Int) {
        playlist.trackIndex = trackId
        play()
    }

    private fun play() {
        mediaPlayer.play()
        showBottomMediaPlayerControl()
    }

    private fun pause() {
        audioFilesAdapter.setActiveTrackIndex(-1)
        mediaPlayer.pause()
    }

    override fun getMediaPlayerCallbacks(): MediaPlayerCallbacks {
        return mediaCallbacks
    }
}
