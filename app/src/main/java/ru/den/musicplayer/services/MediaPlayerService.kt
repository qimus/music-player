package ru.den.musicplayer.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
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
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.media.session.MediaButtonReceiver
import org.koin.android.ext.android.inject
import ru.den.musicplayer.R
import ru.den.musicplayer.createNotificationBuilder
import ru.den.musicplayer.models.CurrentPlaylist
import ru.den.musicplayer.models.Track
import ru.den.musicplayer.ui.MainActivity
import java.util.*

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
    private lateinit var mediaSession: MediaSessionCompat

    private val currentPlaylist: CurrentPlaylist by inject()

    private val stateBuilder: PlaybackStateCompat.Builder = PlaybackStateCompat.Builder()
        .setActions(
            PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_STOP or
                    PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_PLAY_PAUSE or
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        )

    private var timer: Timer? = null
    private lateinit var audioManager: AudioManager
    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener {
        when (it) {
            AudioManager.AUDIOFOCUS_GAIN ->
                mediaSessionCallback.onPlay()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK ->
                mediaSessionCallback.onPause()
            else ->
                mediaSessionCallback.onPause()
        }
    }

    val focusLock = Any()
    var focusRequest: AudioFocusRequest? = null

    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {
        private lateinit var lastTrack: Track

        fun startTimer() {
            stopTimer()

            timer = Timer()
            timer!!.schedule(object : TimerTask() {
                override fun run() {
                    try {
                        player?.let {
                            currentPlaylist.trackProgress = it.currentPosition
                            val playbackState = stateBuilder
                                .setState(
                                    PlaybackStateCompat.STATE_PLAYING,
                                    it.currentPosition.toLong(),
                                    1f
                                )
                                .setExtras(Bundle().apply {
                                    putInt(
                                        EXTRA_TRACK_ID,
                                        currentPlaylist.currentTrackIndex
                                    )
                                })
                                .build()
                            mediaSession.setPlaybackState(playbackState)
                        }
                    } catch (e: Exception) {
                        Log.d(TAG, "Ошибка при отправке тек позиции трека: ${e.message}")
                        e.printStackTrace()
                    }
                }
            }, 0, 1000)
        }

        fun stopTimer() {
            timer?.cancel()
            timer = null
        }

        override fun onPlay() {
            val audioFocusResult = requestAudioFocus()

            synchronized(focusLock) {
                if (audioFocusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    return
                }
            }

            currentPlaylist.currentTrack?.let { currentTrack ->
                val metadata = MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentTrack.name)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currentTrack.artist)
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, currentTrack.album)
                    .build()

                mediaSession.setMetadata(metadata)
                mediaSession.isActive = true

                if (player != null && lastTrack != currentTrack) {
                    onStop()
                }

                val playbackState = stateBuilder
                    .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1f)
                    .setExtras(Bundle().apply {
                        putInt(
                            EXTRA_TRACK_ID,
                            currentPlaylist.currentTrackIndex
                        )
                    })
                    .build()
                mediaSession.setPlaybackState(playbackState)

                if (player == null) {
                    player = MediaPlayer()
                    player!!.setDataSource(applicationContext, currentTrack.getUri())
                    player!!.setOnPreparedListener { it.start() }
                    player!!.prepareAsync()
                    player!!.setOnCompletionListener {
                        currentPlaylist.nextTrack()
                        this.onPlay()
                    }
                    lastTrack = currentTrack
                } else {
                    player!!.start()
                }
                currentPlaylist.isPlaying = true
                startTimer()
                updateForegroundNotification(PlaybackStateCompat.STATE_PLAYING)
            }
        }

        override fun onPause() {
            player?.pause()
            stopTimer()
            currentPlaylist.isPlaying = false

            val playbackState = stateBuilder
                .setState(
                    PlaybackStateCompat.STATE_PAUSED,
                    PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                    1f
                )
                .setExtras(Bundle().apply { putInt(EXTRA_TRACK_ID, currentPlaylist.currentTrackIndex) })
                .build()

            mediaSession.setPlaybackState(playbackState)
            updateForegroundNotification(PlaybackStateCompat.STATE_PAUSED)
        }

        override fun onStop() {
            player?.let {
                it.stop()
                it.release()
            }
            currentPlaylist.isPlaying = false
            player = null
            abandonAudioFocus()

            mediaSession.isActive = false
            val playbackState = stateBuilder
                .setState(
                    PlaybackStateCompat.STATE_STOPPED,
                    PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1f
                )
                .setExtras(Bundle().apply { putInt(EXTRA_TRACK_ID, currentPlaylist.currentTrackIndex) })
                .build()

            mediaSession.setPlaybackState(playbackState)
            updateForegroundNotification(PlaybackStateCompat.STATE_STOPPED)
        }

        override fun onSkipToNext() {
            val playbackState = stateBuilder
                .setState(
                    PlaybackStateCompat.STATE_SKIPPING_TO_NEXT,
                    player?.currentPosition?.toLong()
                        ?: PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                    1f
                )
                .setExtras(Bundle().apply { putInt(EXTRA_TRACK_ID, currentPlaylist.currentTrackIndex) })
                .build()

            mediaSession.setPlaybackState(playbackState)

            onStop()
            currentPlaylist.nextTrack()
            onPlay()
        }

        override fun onSkipToPrevious() {
            val playbackState = stateBuilder
                .setState(
                    PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS,
                    player?.currentPosition?.toLong()
                        ?: PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                    1f
                )
                .setExtras(Bundle().apply { putInt(EXTRA_TRACK_ID, currentPlaylist.currentTrackIndex) })
                .build()

            mediaSession.setPlaybackState(playbackState)

            onStop()
            currentPlaylist.prevTrack()
            onPlay()
        }

        override fun onSeekTo(pos: Long) {
            player?.seekTo(pos.toInt())
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

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        mediaSession = MediaSessionCompat(this, "MediaPlayer")
        mediaSession.setCallback(mediaSessionCallback)
        val activityIntent = Intent(applicationContext, MainActivity::class.java)
        mediaSession.setSessionActivity(
            PendingIntent.getActivity(applicationContext, 0, activityIntent, 0)
        )

        val mediaButtonIntent = Intent(
            Intent.ACTION_MEDIA_BUTTON,
            null, applicationContext, MediaPlayerService::class.java
        )
        mediaSession.setMediaButtonReceiver(
            PendingIntent.getBroadcast(applicationContext, 0, mediaButtonIntent, 0)
        )
    }

    private fun requestAudioFocus(): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest =
                focusRequest ?: AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                    setAudioAttributes(AudioAttributes.Builder().run {
                        setUsage(AudioAttributes.USAGE_MEDIA)
                        setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        build()
                    })
                    setAcceptsDelayedFocusGain(true)
                    setOnAudioFocusChangeListener(audioFocusChangeListener)
                    build()
                }

            return audioManager.requestAudioFocus(focusRequest!!)
        }

        return audioManager.requestAudioFocus(
            audioFocusChangeListener,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            audioManager.abandonAudioFocus(audioFocusChangeListener)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val notificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel =
            NotificationChannel(CHANNEL_ID, "Media playback", NotificationManager.IMPORTANCE_LOW)
        channel.setShowBadge(false)
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        notificationManager.createNotificationChannel(channel)
    }

    private fun updateForegroundNotification(playbackState: Int) {
        when (playbackState) {
            PlaybackStateCompat.STATE_PLAYING ->
                startForeground(NOTIFICATION_ID, createNotification(playbackState))
            PlaybackStateCompat.STATE_PAUSED -> {
                NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, createNotification(playbackState))
                stopForeground(false)
            }
            else -> stopForeground(true)
        }
    }

    private fun createNotification(playbackState: Int): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }

        val notificationBuilder = mediaSession.createNotificationBuilder(this, CHANNEL_ID)
        notificationBuilder
            .addAction(
                NotificationCompat.Action(
                    android.R.drawable.ic_media_previous, "Пред",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this,
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                    )
                )
            )

        if (playbackState == PlaybackStateCompat.STATE_PLAYING) {
            notificationBuilder.addAction(
                NotificationCompat.Action(
                    android.R.drawable.ic_media_pause, "Пауза",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PAUSE)
                )
            )
        } else {
            notificationBuilder.addAction(
                NotificationCompat.Action(
                    android.R.drawable.ic_media_play, "Воспр",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY)
                )
            )
        }

        notificationBuilder.addAction(
            NotificationCompat.Action(
                android.R.drawable.ic_media_next, "След",
                MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
            )
        )

        val mediaStyle = androidx.media.app.NotificationCompat.DecoratedMediaCustomViewStyle()
            .setShowActionsInCompactView(1)
            .setShowCancelButton(true)
            .setCancelButtonIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_STOP)
            )
            .setMediaSession(mediaSession.sessionToken)

        return notificationBuilder.run {
            setStyle(mediaStyle)
            setSmallIcon(R.drawable.ic_music_note)
            color = ContextCompat.getColor(this@MediaPlayerService, R.color.colorPrimaryDark)
            setShowWhen(false)
            priority = NotificationCompat.PRIORITY_HIGH
            build()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSelf()
        stopForeground(true)
        timer?.cancel()
        timer = null
    }
}
