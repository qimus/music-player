package ru.den.musicplayer.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import ru.den.musicplayer.database.models.PlaylistItem

@Dao
interface PlaylistItemDao {
    @Insert
    fun insert(item: PlaylistItem)

    @Update
    fun update(item: PlaylistItem)

    @Query("SELECT * FROM playlist_item WHERE playlist_id = :playlistId")
    fun getItemsByPlaylist(playlistId: Int): LiveData<List<PlaylistItem>>
}
