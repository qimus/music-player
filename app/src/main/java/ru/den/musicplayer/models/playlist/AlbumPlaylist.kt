package ru.den.musicplayer.models.playlist

import android.content.Context
import android.provider.MediaStore
import ru.den.musicplayer.models.Track

class AlbumPlaylist(private val context: Context) : Playlist {

    override val title: String
        get() = "Альбомы"

    override val tracks: List<Track> by lazy {
        fetchTracks()
    }

    override var currentTrackInd: Int = 0

    override val currentTrack: Track?
        get() = tracks[currentTrackInd]

    override var trackProgress: Int = 0

    private fun fetchTracks(): List<Track> {
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
