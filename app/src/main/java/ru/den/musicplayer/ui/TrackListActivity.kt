package ru.den.musicplayer.ui

import android.Manifest
import android.animation.ValueAnimator
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import ru.den.musicplayer.R
import ru.den.musicplayer.models.Track
import ru.den.musicplayer.services.MediaPlayerService
import ru.den.musicplayer.utils.Playlist

interface MediaPlayerListener {
    fun markAsPlaying(trackId: Int)
    fun pause()
}

class TrackListActivity : AppCompatActivity(), TrackListFragment.Player {

    companion object {
        private const val TAG = "TrackListActivity"
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        private const val PERMISSION_CODE = 1
    }

    private var mediaController: MediaControllerCompat? = null
    private var lastState: PlaybackStateCompat? = null

    private var serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {

        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val playerServiceBinder = service as MediaPlayerService.PlayerServiceBinder
            try {
                mediaController = MediaControllerCompat(this@TrackListActivity,
                    playerServiceBinder.getMediaSessionToken())

                mediaController!!.registerCallback(object : MediaControllerCompat.Callback() {
                    override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                        if (state == null) {
                            return
                        }

                        updateMiniPlayerAction(state)

                        when (state.state) {
                            PlaybackStateCompat.STATE_PLAYING -> {
                                val trackId = state.extras?.getInt("TRACK_ID") ?: -1
                                if (trackId > -1 && lastState?.state != state.state) {
                                    getMediaPlayerListener()?.markAsPlaying(trackId)

                                }
                                progress.max = Playlist.currentTrack?.duration ?: 0
                                progress.progress = state.position.toInt()

                                elapsedTime.text = "${Track.formatTrackTime(progress.progress)}/${Track.formatTrackTime(progress.max)}"

                                Log.d(TAG, "max: ${Playlist.currentTrack?.duration}, progress: ${state.position}")
                            }
                            PlaybackStateCompat.STATE_STOPPED -> {
                                Log.d(TAG, "stopped")
                            }
                            PlaybackStateCompat.STATE_PAUSED -> {
                                state.extras?.getInt("TRACK_ID")?.let {
                                    getMediaPlayerListener()?.pause()
                                }
                                Log.d(TAG, "paused")
                            }
                        }

                        lastState = state
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!hasAllPermissions()) {
            requestPermissions()
        }

        MediaPlayerService.startService(applicationContext)
        Playlist.setup(applicationContext)
        bindMediaPlayer()
        bindFragment()
        configureBottomMediaPlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }

    private fun bindFragment() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        if (fragment == null) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragmentContainer, TrackListFragment.newInstance())
                .commit()
        }
    }

    private fun bindMediaPlayer() {
        bindService(Intent(this, MediaPlayerService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun hasAllPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            return ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this,
            REQUIRED_PERMISSIONS,
            PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_CODE -> {
                if (!hasAllPermissions()) {
                    Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    private fun updateMiniPlayerAction(state: PlaybackStateCompat) {
        if (state.state == PlaybackStateCompat.STATE_PLAYING) {
            musicAction.setImageResource(R.drawable.ic_bottom_pause)
        } else {
            musicAction.setImageResource(R.drawable.ic_bottom_play)
        }
        trackTitle.text = Playlist.currentTrack?.name
    }

    private fun configureBottomMediaPlayer() {
        musicAction.setOnClickListener {
            val curState = lastState?.state ?: 0
            if (curState == PlaybackStateCompat.STATE_PLAYING) {
                pause()
            } else {
                play(Playlist.trackIndex)
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
    }

    private fun getMediaPlayerListener(): MediaPlayerListener? {
        return supportFragmentManager.findFragmentById(R.id.fragmentContainer) as? MediaPlayerListener
    }

    override fun play(trackId: Int) {
        Playlist.trackIndex = trackId
        mediaController?.transportControls?.play()
        showBottomMediaPlayerControl()
    }

    override fun pause() {
        mediaController?.transportControls?.pause()
    }
}
