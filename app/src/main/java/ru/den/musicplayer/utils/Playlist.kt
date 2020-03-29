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

    lateinit var tracks: List<Track>

    val currentTrack: Track?
        get() {
            return tracks[trackIndex]
        }

    var trackIndex = 0

    val size: Int
        get() = tracks.size

    fun setup(context: Context) {
        tracks = getAudioFilesFromDevice(context)
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

                val albumCursor = context.contentResolver.query(
                    MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    arrayOf(MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART),
                    MediaStore.Audio.Albums._ID + "=?", arrayOf(it.getString(5)), null)

                if (albumCursor!!.moveToFirst()) {
                    val path = albumCursor.getString(1)
                    Log.d(TAG, "path = $path")
                }

                albumCursor.close()

                fileList.add(audioFile)
            }
            cursor.close()
        }

        return fileList
    }
}
