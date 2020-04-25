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
import ru.den.musicplayer.R
import ru.den.musicplayer.searcher.AlbumSearcher
import ru.den.musicplayer.services.MediaPlayerService

interface MediaPlayer {
    fun play()
    fun pause()
    fun nextTrack()
    fun prevTrack()
    fun stop()
    fun seekTo(pos: Int)
    fun registerMediaPlayerCallbacks(mediaPlayerCallbacks: MediaPlayerCallbacks)
    fun unregisterMediaPlayerCallbacks(mediaPlayerCallbacks: MediaPlayerCallbacks)
}

interface MediaPlayerCallbacks {
    fun onStartPlay() {}
    fun onPause() {}
    fun onPlaying(progress: Int) {}
    fun onStop() {}
    fun onNextTrack() {}
    fun onPrevTrack() {}
}

class MediaPlayerCallbacksComposite : MediaPlayerCallbacks {
    private val mediaPlayerCallbacks = mutableSetOf<MediaPlayerCallbacks>()

    fun addMediaPlayerCallbacks(callbacks: MediaPlayerCallbacks) {
        mediaPlayerCallbacks.add(callbacks)
    }

    fun removeMediaPlayerCallbacks(callbacks: MediaPlayerCallbacks) {
        mediaPlayerCallbacks.remove(callbacks)
    }

    override fun onStartPlay() {
        mediaPlayerCallbacks.forEach {
            it.onStartPlay()
        }
    }

    override fun onPause() {
        mediaPlayerCallbacks.forEach {
            it.onPause()
        }
    }

    override fun onPlaying(progress: Int) {
        mediaPlayerCallbacks.forEach {
            it.onPlaying(progress)
        }
    }

    override fun onStop() {
        mediaPlayerCallbacks.forEach {
            it.onStop()
        }
    }

    override fun onNextTrack() {
        mediaPlayerCallbacks.forEach {
            it.onNextTrack()
        }
    }

    override fun onPrevTrack() {
        mediaPlayerCallbacks.forEach {
            it.onPrevTrack()
        }
    }
}

interface BackPressedBehavior {
    fun onBackPressed(): Boolean
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

    private var mediaPlayerCallbacks = MediaPlayerCallbacksComposite()
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
                        mediaPlayerCallbacks.onStartPlay()
                    }
                    mediaPlayerCallbacks.onPlaying(state.position.toInt())
                }
                PlaybackStateCompat.STATE_STOPPED -> {
                    mediaPlayerCallbacks.onStop()
                }
                PlaybackStateCompat.STATE_PAUSED -> {
                    mediaPlayerCallbacks.onPause()
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

        val albumSearcher = AlbumSearcher(this)
        albumSearcher.search(null)

        bindFragment()
        bindMediaPlayerService()
    }

    override fun registerMediaPlayerCallbacks(mediaPlayerCallbacks: MediaPlayerCallbacks) {
        this.mediaPlayerCallbacks.addMediaPlayerCallbacks(mediaPlayerCallbacks)
    }

    override fun unregisterMediaPlayerCallbacks(mediaPlayerCallbacks: MediaPlayerCallbacks) {
        this.mediaPlayerCallbacks.removeMediaPlayerCallbacks(mediaPlayerCallbacks)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindMediaPlayerService()
    }

    override fun onBackPressed() {
        var isHandled = false
        supportFragmentManager.fragments.forEach {
            if (it is BackPressedBehavior) {
                isHandled = it.onBackPressed()
                if (isHandled) {
                    return
                }
            }
        }

        if (!isHandled) {
            super.onBackPressed()
        }
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
