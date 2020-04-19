package ru.den.musicplayer.searcher

import android.provider.MediaStore
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before

class SelectionBuilderTest {
    private lateinit var selectionBuilder: SelectionBuilder

    @Before
    fun setUp() {
        selectionBuilder = SelectionBuilder()
    }

    @Test
    fun addSelection() {
        selectionBuilder.addSelection("${MediaStore.Audio.Media.ALBUM_ID} = ?", arrayOf("album_id"))
        selectionBuilder.addSelection("${MediaStore.Audio.Media.ARTIST_ID} = ?", arrayOf("artist_id", "some_id"))

        var (selection, selectionArgs) = selectionBuilder.getQuery()

        assertEquals("album_id = ? AND artist_id = ?", selection)
        assertNotNull(selectionArgs)
        assertEquals(3, selectionArgs!!.size)
        assertEquals("album_id", selectionArgs[0])
        assertEquals("artist_id", selectionArgs[1])
    }
}
