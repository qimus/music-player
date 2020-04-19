package ru.den.musicplayer.models

import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import java.io.Serializable

data class Track(
    var id: Int,
    var name: String,
    var cover: Bitmap? = null,
    var album: String? = null,
    var artist: String? = null,
    var duration: Int? = null
) : Serializable {
    companion object {
        fun formatTrackTime(time: Int): String {
            val inSeconds = time / 1000
            val minutes = inSeconds / 60
            var seconds = inSeconds % 60

            return "$minutes:${seconds.toString().padStart(2,'0')}"
        }
    }

    fun getUri(): Uri {
        return Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "" + id)
    }

    fun getFormattedDuration(): String {
        duration?.let {
            return formatTrackTime(it)
        }

        return ""
    }
}
