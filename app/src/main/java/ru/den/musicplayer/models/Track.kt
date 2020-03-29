package ru.den.musicplayer.models

import android.content.Context
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
    fun getUri(): Uri {
        return Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "" + id)
    }

    fun getFormattedDuration(): String {
        duration?.let {
            val inSeconds = it / 1000
            val minutes = inSeconds / 60
            var seconds = inSeconds % 60

            if (minutes == 0 && seconds == 0) {
                seconds = 1
            }

            return "$minutes:${seconds.toString().padStart(2,'0')}"
        }

        return ""
    }
}
