package ru.den.musicplayer.searcher

import android.content.Context
import android.os.Parcelable
import android.provider.MediaStore
import kotlinx.android.parcel.Parcelize
import ru.den.musicplayer.models.Album
import ru.den.musicplayer.models.Artist
import ru.den.musicplayer.models.Track
import ru.den.musicplayer.models.Year

@Parcelize
data class MusicSearchCriteria(
    var albumId: String? = null,
    var artistId: String? = null,
    var year: Int? = null
) : Parcelable

interface Searcher<T, R> {
    fun search(criteria: T): R
}

class SelectionBuilder {
    private val queryParts = mutableListOf<Map<String, Array<String>>>()

    fun addSelection(criteria: String, value: Array<String>) {
        queryParts.add(mapOf(criteria to value))
    }

    fun getQuery(): Pair<String?, Array<String>?> {
        if (queryParts.size == 0) {
            return Pair(null, null)
        }

        val selection = mutableListOf<String>()
        val selectionArgs = mutableListOf<String>()

        for (queryPart in queryParts) {
            for ((key, value) in queryPart) {
                selection.add(key)
                selectionArgs.addAll(value)
            }
        }

        return Pair(
            selection.joinToString(separator = " AND "),
            selectionArgs.toTypedArray()
        )
    }
}

class TrackSearcher(val context: Context) : Searcher<MusicSearchCriteria?, List<Track>>{
    override fun search(criteria: MusicSearchCriteria?): List<Track> {
        val fileList = mutableListOf<Track>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.AudioColumns._ID, MediaStore.Audio.AudioColumns.DISPLAY_NAME,
            MediaStore.Audio.AudioColumns.ALBUM, MediaStore.Audio.AudioColumns.ARTIST,
            MediaStore.Audio.AudioColumns.DURATION, MediaStore.Audio.AudioColumns.ALBUM_ID
        )

        val selectionBuilder = SelectionBuilder()

        criteria?.albumId?.let {
            selectionBuilder.addSelection("${MediaStore.Audio.Media.ALBUM_ID} = ?", arrayOf(it))
        }

        criteria?.artistId?.let {
            selectionBuilder.addSelection("${MediaStore.Audio.Media.ARTIST_ID} = ?", arrayOf(it))
        }

        criteria?.year?.let {
            selectionBuilder.addSelection("${MediaStore.Audio.Media.YEAR} = ?", arrayOf(it.toString()))
        }

        val (selection, selectionArgs) = selectionBuilder.getQuery()

        val cursor = context.contentResolver.query(uri,
            projection, selection, selectionArgs, null)

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

class AlbumSearcher(val context: Context) : Searcher<Unit?, List<Album>> {
    private val foundAlbums = mutableSetOf<String>()

    override fun search(criteria: Unit?): List<Album> {
        foundAlbums.clear()
        val albums = mutableListOf<Album>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.AudioColumns.ALBUM_ID,
            MediaStore.Audio.AudioColumns.ALBUM,
            MediaStore.Audio.AudioColumns.ALBUM_KEY
        )

        val cursor = context.contentResolver.query(uri,
            projection, null, null, null)

        cursor?.let {
            while (it.moveToNext()) {
                val albumId = it.getString(0)
                if (foundAlbums.indexOf(albumId) == -1) {
                    val album = Album(
                        id = albumId,
                        name = it.getString(1),
                        key = it.getString(2)
                    )
                    albums.add(album)
                    foundAlbums.add(albumId)
                }
            }
            cursor.close()
        }

        return albums
    }
}

class ArtistSearcher(val context: Context) : Searcher<Unit?, List<Artist>> {
    private val foundArtists = mutableSetOf<String>()

    override fun search(criteria: Unit?): List<Artist> {
        foundArtists.clear()
        val artists = mutableListOf<Artist>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.AudioColumns.ARTIST_ID,
            MediaStore.Audio.AudioColumns.ARTIST,
            MediaStore.Audio.AudioColumns.ARTIST_KEY
        )

        val cursor = context.contentResolver.query(uri,
            projection, null, null, null)

        cursor?.let {
            while (it.moveToNext()) {
                val artistId = it.getString(0)
                if (foundArtists.indexOf(artistId) == -1) {
                    val artist = Artist(
                        id = artistId,
                        name = it.getString(1),
                        key = it.getString(2)
                    )
                    artists.add(artist)
                    foundArtists.add(artistId)
                }
            }
            cursor.close()
        }

        return artists
    }
}

class YearSearcher(val context: Context) : Searcher<Unit?, List<Year>> {
    private val foundYears = mutableSetOf<String>()

    override fun search(criteria: Unit?): List<Year> {
        foundYears.clear()
        val years = mutableListOf<Year>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.AudioColumns.YEAR
        )

        val cursor = context.contentResolver.query(uri,
            projection, null, null, null)

        cursor?.let {
            while (it.moveToNext()) {
                val yearValue: String? = it.getString(0)
                if (yearValue != null && foundYears.indexOf(yearValue) == -1) {
                    years.add(Year(yearValue))
                    foundYears.add(yearValue)
                }
            }
            cursor.close()
        }

        return years
    }
}
