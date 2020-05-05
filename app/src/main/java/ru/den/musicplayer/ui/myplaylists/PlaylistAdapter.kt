package ru.den.musicplayer.ui.myplaylists

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.den.musicplayer.database.models.Playlist

class PlaylistAdapter(private val items: MutableList<Playlist> = mutableListOf())
    : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    fun updateItems(items: List<Playlist>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }
}
