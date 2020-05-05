package ru.den.musicplayer.ui.myplaylists

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.den.musicplayer.R
import ru.den.musicplayer.database.models.Playlist
import ru.den.musicplayer.inflate

class PlaylistAdapter(private val items: MutableList<Playlist> = mutableListOf())
    : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    fun updateItems(items: List<Playlist>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        return PlaylistViewHolder(parent.inflate(R.layout.playlist_item, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(items[position])
    }

    class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(playlist: Playlist) {

        }
    }
}
