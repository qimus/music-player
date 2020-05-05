package ru.den.musicplayer.ui.myplaylists.chooseTracks

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.dialog_choose_track_item.view.*
import ru.den.musicplayer.R
import ru.den.musicplayer.inflate
import ru.den.musicplayer.models.Track

class TracksAdapter(private val items: MutableList<Track> = mutableListOf()) : RecyclerView.Adapter<TracksAdapter.TrackViewHolder>() {
    fun updateItems(items: List<Track>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        return TrackViewHolder(parent.inflate(R.layout.dialog_choose_track_item, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(items[position])
    }

    class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(track: Track) {
            itemView.name.text = track.name
        }
    }
}
