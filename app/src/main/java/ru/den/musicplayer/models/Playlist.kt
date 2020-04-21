package ru.den.musicplayer.models

class Playlist {
    private val _tracks = mutableListOf<Track>();
    val tracks: List<Track>
        get() = _tracks

    var isPlaying = false
    var trackProgress = 0
    var playlistName: String = ""
    var currentTrackIndex = 0
    val currentTrack: Track?
        get() = if (tracks.isNotEmpty()) tracks[currentTrackIndex] else null

    fun setTracks(tracks: List<Track>) {
        _tracks.clear()
        _tracks.addAll(tracks)
        currentTrackIndex = 0
    }

    fun addTracks(tracks: List<Track>) {
        _tracks.addAll(tracks)
    }

    fun nextTrack() {
        if (currentTrackIndex < tracks.size - 1) {
            currentTrackIndex++
        } else {
            currentTrackIndex = 0
        }
    }

    fun prevTrack() {
        if (currentTrackIndex > 0) {
            currentTrackIndex--
        }
    }
}
