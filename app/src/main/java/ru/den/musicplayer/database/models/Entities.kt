package ru.den.musicplayer.database.models

import androidx.room.*

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true)
    var id: Int,

    var name: String) {
}

@Entity(tableName = "playlist_item")
data class PlaylistItem(
    @PrimaryKey(autoGenerate = true)
    var id: Int,

    @ColumnInfo(name = "playlist_id")
    var playlistId: Int,

    @ColumnInfo(name = "track_id")
    var trackId: Int,

    var title: String
) {
}

data class PlaylistItemWithPlaylist(
    @Embedded
    val playlist: Playlist,

    @Relation(
        parentColumn = "id",
        entityColumn = "playlist_id"
    )
    val items: List<PlaylistItem>
) {

}
