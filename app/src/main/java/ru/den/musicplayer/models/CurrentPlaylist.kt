package ru.den.musicplayer.models

class CurrentPlaylist {
    private val _tracks = mutableListOf<Track>();
    val tracks: List<Track>
        get() = _tracks

    var isPlaying = false
        set(value) {
            synchronized(this) {
                field = value
            }
        }
    var trackProgress = 0
    var playlistName: String = ""
    var currentTrackIndex = 0
    val currentTrack: Track?
        get() = if (tracks.isNotEmpty()) tracks[currentTrackIndex] else null

    fun setTracks(tracks: List<Track>) {
        synchronized(this) {
            _tracks.clear()
            _tracks.addAll(tracks)
            currentTrackIndex = 0
        }
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
