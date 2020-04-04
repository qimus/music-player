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
import ru.den.musicplayer.services.MediaPlayerService
import ru.den.musicplayer.utils.Playlist

interface MediaPlayerListener {
    fun play(trackId: Int)
    fun pause(trackId: Int)
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

    private var serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            TODO("Not yet implemented")
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

                        when (state.state) {
                            PlaybackStateCompat.STATE_PLAYING -> {
                                state.extras?.getInt("TRACK_ID")?.let {
                                    getMediaPlayerListener()?.play(it)
                                }
                                Log.d(TAG, "playing, trackId: ${state.extras?.getInt("TRACK_ID")}")
                            }
                            PlaybackStateCompat.STATE_STOPPED -> {
                                Log.d(TAG, "stopped")
                            }
                            PlaybackStateCompat.STATE_PAUSED -> {
                                state.extras?.getInt("TRACK_ID")?.let {
                                    getMediaPlayerListener()?.pause(it)
                                }
                                Log.d(TAG, "paused")
                            }
                        }
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

        Playlist.setup(applicationContext)
        bindMediaPlayer()
        MediaPlayerService.startService(applicationContext)
        bindFragment()
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

    private fun getMediaPlayerListener(): MediaPlayerListener? {
        return supportFragmentManager.findFragmentById(R.id.fragmentContainer) as MediaPlayerListener
    }

    override fun play(trackId: Int) {
        Playlist.trackIndex = trackId
        mediaController?.transportControls?.play()
    }

    override fun pause(trackId: Int) {
        mediaController?.transportControls?.pause()
    }
}
