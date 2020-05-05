package ru.den.musicplayer.ui.myplaylists.create

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.track_item.view.*
import ru.den.musicplayer.R
import ru.den.musicplayer.inflate
import ru.den.musicplayer.models.Track

class SelectedTracksAdapter(private val items: MutableList<Track> = mutableListOf())
    : RecyclerView.Adapter<SelectedTracksAdapter.SelectedTrackViewHolder>()
{
    fun updateItems(items: List<Track>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedTrackViewHolder {
        return SelectedTrackViewHolder(parent.inflate(R.layout.track_item, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: SelectedTrackViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class SelectedTrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(track: Track) {
            itemView.name.text = track.name
            itemView.album.text = track.album
        }
    }
}
