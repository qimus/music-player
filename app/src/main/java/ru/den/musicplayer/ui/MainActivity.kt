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
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import ru.den.musicplayer.R
import ru.den.musicplayer.services.MediaPlayerService
import ru.den.musicplayer.utils.Playlist

class MainActivity : AppCompatActivity(), TrackListAdapter.OnTrackListener {

    companion object {
        private const val TAG = "MainActivity"
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        private const val PERMISSION_CODE = 1
    }

    private val audioFilesAdapter = TrackListAdapter(mutableListOf(), this)

    private var mediaController: MediaControllerCompat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!hasAllPermissions()) {
            requestPermissions()
        }

        val files = Playlist.getAudioFilesFromDevice(this)
        audioFilesAdapter.updateItems(files)

        fileListRecyclerView.adapter = audioFilesAdapter
        fileListRecyclerView.layoutManager = LinearLayoutManager(this)

        Playlist.setup(this)
        bindMediaPlayer()

        MediaPlayerService.play(this)
    }

    private fun bindMediaPlayer() {
        bindService(Intent(this, MediaPlayerService::class.java), object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {
                TODO("Not yet implemented")
            }

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val playerServiceBinder = service as MediaPlayerService.PlayerServiceBinder
                try {
                    mediaController = MediaControllerCompat(this@MainActivity,
                        playerServiceBinder.getMediaSessionToken())

                    mediaController!!.registerCallback(object : MediaControllerCompat.Callback() {
                        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                            if (state == null) {
                                return
                            }

                            val playing = state.state == PlaybackStateCompat.STATE_PLAYING
                        }
                    })
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }, Context.BIND_AUTO_CREATE)
    }

    override fun playTrack(trackId: Int) {
        Playlist.trackIndex = trackId
        mediaController?.transportControls?.play()
        audioFilesAdapter.setPlayingTrackIndex(trackId)
    }

    override fun pauseTrack(trackId: Int) {
        mediaController?.transportControls?.pause()
        audioFilesAdapter.setPlayingTrackIndex(-1)
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
}
