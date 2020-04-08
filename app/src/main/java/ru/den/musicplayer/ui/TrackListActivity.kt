package ru.den.musicplayer.ui

import android.Manifest
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
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import ru.den.musicplayer.R
import ru.den.musicplayer.services.MediaPlayerService

interface MediaPlayer {
    fun play()
    fun pause()
    fun nextTrack()
    fun prevTrack()
    fun stop()
    fun seekTo(pos: Int)
}


interface MediaPlayerCallbacks {
    interface Holder {
        fun getMediaPlayerCallbacks() : MediaPlayerCallbacks
    }

    fun onStartPlay()
    fun onPause()
    fun onPlaying(progress: Int)
    fun onStop()
    fun onNextTrack()
    fun onPrevTrack()
}

class TrackListActivity : AppCompatActivity(), MediaPlayer {

    companion object {
        private const val TAG = "TrackListActivity"
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        private const val PERMISSION_CODE = 1
    }

    private var mediaPlayerCallbacks: MediaPlayerCallbacks? = null

    private var mediaController: MediaControllerCompat? = null
    private var lastState: PlaybackStateCompat? = null
    private val mediaControllerCallback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            if (state == null ) {
                return
            }

            when (state.state) {
                PlaybackStateCompat.STATE_PLAYING -> {
                    val trackId = state.extras?.getInt("TRACK_ID") ?: -1
                    if (trackId > -1 && lastState?.state != state.state) {
                        mediaPlayerCallbacks?.onStartPlay()
                    }
                    mediaPlayerCallbacks?.onPlaying(state.position.toInt())
                }
                PlaybackStateCompat.STATE_STOPPED -> {
                    mediaPlayerCallbacks?.onStop()
                }
                PlaybackStateCompat.STATE_PAUSED -> {
                    mediaPlayerCallbacks?.onPause()
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
                mediaController = MediaControllerCompat(applicationContext,
                    playerServiceBinder.getMediaSessionToken())

                mediaController!!.registerCallback(mediaControllerCallback)
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

        bindFragment()
        bindMediaPlayerService()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindMediaPlayerService()
    }

    private fun bindMediaPlayerService() {
        bindService(
            Intent(applicationContext, MediaPlayerService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    private fun unbindMediaPlayerService() {
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

    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)
        if (fragment is MediaPlayerCallbacks.Holder) {
            mediaPlayerCallbacks = fragment.getMediaPlayerCallbacks()
        }
    }

    override fun play() {
        Log.d(TAG, "Play")
        mediaController?.transportControls?.play()
    }

    override fun pause() {
        mediaController?.transportControls?.pause()
    }

    override fun nextTrack() {
        mediaController?.transportControls?.skipToNext()
    }

    override fun prevTrack() {
        mediaController?.transportControls?.skipToPrevious()
    }

    override fun stop() {
        mediaController?.transportControls?.stop()
    }

    override fun seekTo(pos: Int) {
        mediaController?.transportControls?.seekTo(pos.toLong())
    }
}
