package ru.den.musicplayer.database

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.den.musicplayer.database.dao.PlaylistDao
import ru.den.musicplayer.database.dao.PlaylistItemDao
import ru.den.musicplayer.database.models.Playlist
import ru.den.musicplayer.database.models.PlaylistItem

@Database(entities = [Playlist::class, PlaylistItem::class], version = 1)
abstract class MusicDatabase : RoomDatabase() {
    abstract val playlistDao: PlaylistDao
    abstract val playlistItemDao: PlaylistItemDao
}
