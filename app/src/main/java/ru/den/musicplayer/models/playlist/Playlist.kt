package ru.den.musicplayer.models.playlist

import ru.den.musicplayer.models.Track

interface Playlist {
    val title: String
    var currentTrackInd: Int
    val currentTrack: Track?
    val tracks: List<Track>
    var trackProgress: Int

    fun nextTrack() {
        if (currentTrackInd < tracks.size - 1) {
            currentTrackInd++
        }
    }

    fun prevTrack() {
        if (currentTrackInd > 0) {
            currentTrackInd--
        }
    }
}
