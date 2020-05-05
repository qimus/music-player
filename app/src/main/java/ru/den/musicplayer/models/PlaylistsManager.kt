package ru.den.musicplayer.models

import android.content.res.Resources
import androidx.fragment.app.Fragment
import ru.den.musicplayer.R
import ru.den.musicplayer.searcher.AlbumSearcher
import ru.den.musicplayer.searcher.ArtistSearcher
import ru.den.musicplayer.searcher.YearSearcher
import ru.den.musicplayer.ui.ListFragment
import ru.den.musicplayer.ui.TracksFragment
import ru.den.musicplayer.ui.adapters.ListAdapter
import ru.den.musicplayer.ui.myplaylists.PlaylistFragment

interface PlaylistType {
    val title: String
    fun createFragment(): Fragment
}

class AllTracksPlaylist: PlaylistType {
    override val title: String = "Все песни"

    override fun createFragment(): Fragment {
        return TracksFragment.newInstance(null, "all")
    }
}

class AlbumPlaylist: PlaylistType {
    override val title: String = "Альбомы"

    override fun createFragment(): Fragment {
        return ListFragment.newInstance<Album, AlbumSearcher>(ListAdapter(), "album")
    }
}

class ArtistPlaylist: PlaylistType {
    override val title: String = "Исполнители"

    override fun createFragment(): Fragment {
        return ListFragment.newInstance<Artist, ArtistSearcher>(ListAdapter(), "artist")
    }
}

class YearPlaylist: PlaylistType {
    override val title: String = "Год"

    override fun createFragment(): Fragment {
        return ListFragment.newInstance<Year, YearSearcher>(ListAdapter(), "year")
    }
}

class MyPlaylists(private val resources: Resources): PlaylistType {
    override val title: String
        get() = resources.getString(R.string.my_playlists)

    override fun createFragment(): Fragment {
        return PlaylistFragment.newInstance()
    }
}

class PlaylistsManager {
    private val _playlistTypes = mutableListOf<PlaylistType>()
    val playlistTypes: List<PlaylistType>
        get() = _playlistTypes

    fun addPlaylistType(playlistType: PlaylistType) {
        _playlistTypes.add(playlistType)
    }
}
