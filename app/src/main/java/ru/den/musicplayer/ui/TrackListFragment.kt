package ru.den.musicplayer.ui

import android.animation.ValueAnimator
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_track_list.*
import kotlinx.android.synthetic.main.fragment_track_list.elapsedTime
import kotlinx.android.synthetic.main.fragment_track_list.progress
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
class TrackListFragment : Fragment(), TrackListAdapter.OnTrackListener {

    companion object {
        private const val TAG = "TrackListFragment"
        @JvmStatic
        fun newInstance() = TrackListFragment()
        private var bottomPlayerIsVisible = false
    }

    private val playlist: Playlist by inject()
    private val audioFilesAdapter = TrackListAdapter(mutableListOf(), this)
    private var mediaController: MediaControllerCompat? = null
    private var lastState: PlaybackStateCompat? = null
    private val mediaControllerCallback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            if (state == null || activity == null) {
                return
            }

            updateMiniPlayerAction(state)

            when (state.state) {
                PlaybackStateCompat.STATE_PLAYING -> {
                    val trackId = state.extras?.getInt("TRACK_ID") ?: -1
                    if (trackId > -1 && lastState?.state != state.state) {
                        markAsPlaying(trackId)
                    }
                    progress.max = playlist.currentTrack?.duration ?: 0
                    progress.progress = state.position.toInt()

                    elapsedTime.text = "${Track.formatTrackTime(progress.progress)}/${Track.formatTrackTime(progress.max)}"

                    Log.d(TAG, "max: ${playlist.currentTrack?.duration}, progress: ${state.position}")
                }
                PlaybackStateCompat.STATE_STOPPED -> {
                    Log.d(TAG, "stopped")
                }
                PlaybackStateCompat.STATE_PAUSED -> {
                    Log.d(TAG, "paused")
                }
            }

            lastState = state
        }
    }

    private var serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            mediaController?.unregisterCallback(mediaControllerCallback)
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val playerServiceBinder = service as MediaPlayerService.PlayerServiceBinder
            try {
                mediaController = MediaControllerCompat(context,
                    playerServiceBinder.getMediaSessionToken())

                mediaController!!.registerCallback(mediaControllerCallback)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        context?.let {
            MediaPlayerService.startService(it)
        }
        bindMediaPlayerService()
        configureBottomMediaPlayer()
        if (bottomPlayerIsVisible) {
            showBottomMediaPlayerControl()
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
        unbindMediaPlayerService()
    }

    private fun bindMediaPlayerService() {
        activity?.bindService(
            Intent(context, MediaPlayerService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    private fun unbindMediaPlayerService() {
        activity?.unbindService(serviceConnection)
    }

    private fun updateMiniPlayerAction(state: PlaybackStateCompat) {
        if (state.state == PlaybackStateCompat.STATE_PLAYING) {
            musicAction.setImageResource(R.drawable.ic_bottom_pause)
        } else {
            musicAction.setImageResource(R.drawable.ic_bottom_play)
        }
        trackTitle.text = playlist.currentTrack?.name
    }

    private fun configureBottomMediaPlayer() {
        musicAction.setOnClickListener {
            val curState = lastState?.state ?: 0
            if (curState == PlaybackStateCompat.STATE_PLAYING) {
                pause()
            } else {
                play(playlist.trackIndex)
            }
        }

        next.setOnClickListener {
            mediaController?.transportControls?.skipToNext()
        }

        prev.setOnClickListener {
            mediaController?.transportControls?.skipToPrevious()
        }

        progress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.progress?.let {
                    mediaController?.transportControls?.seekTo(it.toLong())
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
    }

    override fun onTrackSelected(trackId: Int) {
        play(trackId)
    }

    private fun markAsPlaying(trackId: Int) {
        audioFilesAdapter.setActiveTrackIndex(trackId)
    }

    private fun play(trackId: Int) {
        playlist.trackIndex = trackId
        mediaController?.transportControls?.play()
        showBottomMediaPlayerControl()
    }

    private fun pause() {
        audioFilesAdapter.setActiveTrackIndex(-1)
        mediaController?.transportControls?.pause()
    }
}
