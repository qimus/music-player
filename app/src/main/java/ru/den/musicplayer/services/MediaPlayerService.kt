package ru.den.musicplayer.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.media.session.MediaButtonReceiver
import ru.den.musicplayer.models.Track
import ru.den.musicplayer.ui.TrackListActivity
import ru.den.musicplayer.utils.Playlist

class MediaPlayerService : Service() {
    companion object {
        private const val TAG = "MediaPlayerService"

        private const val CHANNEL_ID = "media_playback"
        private const val NOTIFICATION_ID = 1000

        private const val PLAY = "ru.den.musicplayer.PLAY"
        private const val STOP = "ru.den.musicplayer.STOP"
        private const val PAUSE = "ru.den.musicplayer.PAUSE"

        const val EXTRA_TRACK_ID = "TRACK_ID"

        fun startService(context: Context) {
            val intent = Intent(context, MediaPlayerService::class.java)
            context.startService(intent)
        }
    }

    private var player: MediaPlayer? = null
    private var track: Track? = null
    private lateinit var mediaSession: MediaSessionCompat

    private val stateBuilder: PlaybackStateCompat.Builder = PlaybackStateCompat.Builder()
        .setActions(
            PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_STOP or
                    PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_PLAY_PAUSE or
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        )

    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {
        private lateinit var lastTrack: Track

        override fun onPlay() {
            val currentTrack = Playlist.currentTrack
            currentTrack?.let {
                val metadata = MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentTrack.name)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currentTrack.artist)
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, currentTrack.album)
                    .build()

                mediaSession.setMetadata(metadata)
                mediaSession.isActive = true

                val playbackState = stateBuilder
                    .setState(PlaybackStateCompat.STATE_PLAYING,
                        0, 1f)
                    .setExtras(Bundle().apply { putInt(EXTRA_TRACK_ID, Playlist.trackIndex) })
                    .build()

                mediaSession.setPlaybackState(playbackState)

                if (player != null && lastTrack != currentTrack) {
                    player?.stop()
                    player?.release()
                    player = null
                }

                if (player == null) {
                    player = MediaPlayer()
                    player?.setDataSource(applicationContext, currentTrack.getUri())
                    player?.setOnPreparedListener {
                        it.start()
                    }
                    player?.prepareAsync()
                    player?.setOnCompletionListener {
                        Playlist.next()
                        this.onPlay()
                    }
                    lastTrack = currentTrack
                } else {
                    player?.start()
                }
            }
        }

        override fun onPause() {
            player?.pause()

            val playbackState = stateBuilder
                .setState(PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1f)
                .setExtras(Bundle().apply { putInt(EXTRA_TRACK_ID, Playlist.trackIndex) })
                .build()

            mediaSession.setPlaybackState(playbackState)
        }

        override fun onStop() {
            player?.let {
                it.stop()
                it.release()
            }
            player = null

            mediaSession.isActive = false
            val playbackState = stateBuilder
                .setState(PlaybackStateCompat.STATE_STOPPED,
                    PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1f)
                .setExtras(Bundle().apply { putInt(EXTRA_TRACK_ID, Playlist.trackIndex) })
                .build()

            mediaSession.setPlaybackState(playbackState)
        }
    }

    inner class PlayerServiceBinder : Binder() {
        fun getMediaSessionToken(): MediaSessionCompat.Token {
            return mediaSession.sessionToken
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return PlayerServiceBinder()
    }

    override fun onCreate() {
        super.onCreate()

        mediaSession = MediaSessionCompat(this, "MediaPlayer")
        mediaSession.setCallback(mediaSessionCallback)
        val activityIntent = Intent(applicationContext, TrackListActivity::class.java)
        mediaSession.setSessionActivity(
            PendingIntent.getActivity(applicationContext, 0, activityIntent, 0)
        )

        val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON,
            null, applicationContext, MediaPlayerService::class.java)
        mediaSession.setMediaButtonReceiver(
            PendingIntent.getBroadcast(applicationContext, 0, mediaButtonIntent, 0)
        )

        createNotification()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(CHANNEL_ID, "Media playback", NotificationManager.IMPORTANCE_LOW)
        channel.setShowBadge(false)
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }

        val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle()
            .setShowActionsInCompactView(1)
            .setMediaSession(mediaSession.sessionToken)
            .setShowCancelButton(true)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(track?.name)
            .setContentText("Artist - Album")
            .setStyle(mediaStyle)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSelf()
        stopForeground(true)
    }
}
