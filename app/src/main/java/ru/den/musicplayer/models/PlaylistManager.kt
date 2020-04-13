package ru.den.musicplayer.models

import ru.den.musicplayer.models.playlist.Playlist


class PlaylistManager {
    var currentPlaylistInd: Int = 0
    var isPlaying = false
    val playlists = mutableListOf<Playlist>()
    val currentPlaylist: Playlist
        get() = playlists[currentPlaylistInd]

    fun addPlaylist(playlist: Playlist): PlaylistManager {
        playlists.add(playlist)
        return this
    }
}
