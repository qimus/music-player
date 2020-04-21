package ru.den.musicplayer.models

import androidx.fragment.app.Fragment
import ru.den.musicplayer.searcher.AlbumSearcher
import ru.den.musicplayer.searcher.ArtistSearcher
import ru.den.musicplayer.searcher.YearSearcher
import ru.den.musicplayer.ui.ListFragment
import ru.den.musicplayer.ui.TracksFragment
import ru.den.musicplayer.ui.adapters.ListAdapter

interface PlaylistType {
    val title: String
    fun createFragment(): Fragment
}

class AllTracksPlaylistType: PlaylistType {
    override val title: String = "Все песни"

    override fun createFragment(): Fragment {
        return TracksFragment.newInstance(null, "all")
    }
}

class AlbumPlaylistType: PlaylistType {
    override val title: String = "Альбомы"

    override fun createFragment(): Fragment {
        return ListFragment.newInstance<Album, AlbumSearcher>(ListAdapter(), "album")
    }
}

class ArtistPlaylistType: PlaylistType {
    override val title: String = "Исполнители"

    override fun createFragment(): Fragment {
        return ListFragment.newInstance<Artist, ArtistSearcher>(ListAdapter(), "artist")
    }
}

class YearPlaylistType: PlaylistType {
    override val title: String = "Год"

    override fun createFragment(): Fragment {
        return ListFragment.newInstance<Year, YearSearcher>(ListAdapter(), "year")
    }
}

class PlaylistTypeManager {
    private val _playlistTypes = mutableListOf<PlaylistType>()
    val playlistTypes: List<PlaylistType>
        get() = _playlistTypes

    fun addPlaylistType(playlistType: PlaylistType) {
        _playlistTypes.add(playlistType)
    }
}
