package ru.den.musicplayer.models

import androidx.fragment.app.Fragment
import ru.den.musicplayer.searcher.AlbumSearcher
import ru.den.musicplayer.searcher.ArtistSearcher
import ru.den.musicplayer.searcher.YearSearcher
import ru.den.musicplayer.ui.ListFragment
import ru.den.musicplayer.ui.TracksFragment
import ru.den.musicplayer.ui.adapters.ListAdapter

interface Playlist {
    val title: String
    fun createFragment(): Fragment
}

class AllTracksPlaylist: Playlist {
    override val title: String = "Все песни"

    override fun createFragment(): Fragment {
        return TracksFragment.newInstance(null)
    }
}

class AlbumPlaylist: Playlist {
    override val title: String = "Альбомы"

    override fun createFragment(): Fragment {
        return ListFragment.newInstance<Album, AlbumSearcher>(ListAdapter(), "album")
    }
}

class ArtistPlaylist: Playlist {
    override val title: String = "Исполнители"

    override fun createFragment(): Fragment {
        return ListFragment.newInstance<Artist, ArtistSearcher>(ListAdapter(), "artist")
    }
}

class YearPlaylist: Playlist {
    override val title: String = "Год"

    override fun createFragment(): Fragment {
        return ListFragment.newInstance<Year, YearSearcher>(ListAdapter(), "year")
    }
}

class PlaylistManager {
    private val _playlists = mutableListOf<Playlist>()
    val playlists: List<Playlist>
        get() = _playlists

    fun addPlaylist(playlist: Playlist) {
        _playlists.add(playlist)
    }
}
