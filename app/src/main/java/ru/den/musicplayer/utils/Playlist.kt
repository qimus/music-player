package ru.den.musicplayer.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import ru.den.musicplayer.models.Track

object Playlist {
    private const val TAG = "Playlist"

    var tracks = mutableListOf<Track>()

    val currentTrack: Track?
        get() {
            return tracks[trackIndex]
        }

    var trackIndex = 0

    fun setup(context: Context) {
        tracks.addAll(getAudioFilesFromDevice(context))
    }

    fun next() {
        if (trackIndex > tracks.size) {
            trackIndex = 0
        } else {
            trackIndex++
        }
    }

    fun getAudioFilesFromDevice(context: Context): List<Track> {
        val fileList = mutableListOf<Track>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.AudioColumns._ID, MediaStore.Audio.AudioColumns.DISPLAY_NAME,
            MediaStore.Audio.AudioColumns.ALBUM, MediaStore.Audio.AudioColumns.ARTIST,
            MediaStore.Audio.AudioColumns.DURATION, MediaStore.Audio.AudioColumns.ALBUM_ID
        )

        val cursor = context.contentResolver.query(uri,
            projection, null, null, null)

        cursor?.let {
            while (it.moveToNext()) {
                val audioFile = Track(
                    id = it.getInt(0),
                    name = it.getString(1),
                    album = it.getString(2),
                    artist = it.getString(3),
                    duration = it.getInt(4)
                )
                fileList.add(audioFile)
            }
            cursor.close()
        }

        return fileList
    }
}
