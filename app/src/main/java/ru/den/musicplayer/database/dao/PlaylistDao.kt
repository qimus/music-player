package ru.den.musicplayer.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import ru.den.musicplayer.database.models.Playlist

@Dao
interface PlaylistDao {

    @Insert
    fun insert(playlist: Playlist)

    @Update
    fun update(playlist: Playlist)

    @Query("SELECT * FROM playlists")
    fun getPlaylists(): LiveData<List<Playlist>>
}
