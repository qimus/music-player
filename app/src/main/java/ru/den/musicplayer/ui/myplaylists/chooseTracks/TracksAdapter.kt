package ru.den.musicplayer.ui.myplaylists.chooseTracks

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.dialog_choose_track_item.view.*
import ru.den.musicplayer.R
import ru.den.musicplayer.inflate
import ru.den.musicplayer.models.Track

class TracksAdapter(
    private val items: MutableList<Track> = mutableListOf(),
    private val onSelectListener: OnSelectListener
) : RecyclerView.Adapter<TracksAdapter.TrackViewHolder>() {
    val checked = mutableSetOf<String>()

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
        holder.bind(items[position], position)
    }

    inner class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(track: Track, position: Int) {
            itemView.setOnClickListener {
                var selected = false
                if (checked.contains(track.id)) {
                    checked.remove(track.id)
                } else {
                    selected = true
                    checked.add(track.id)
                }
                notifyItemChanged(position)
                onSelectListener.onSelect(track, selected)
            }

            itemView.name.text = track.name
            itemView.album.text = track.album
            itemView.duration.text = track.getFormattedDuration()

            var imageResource = R.drawable.ic_circle_unchecked
            var backgroundColor = android.R.color.white
            if (checked.contains(track.id)) {
                imageResource = R.drawable.ic_circle_check
                backgroundColor = android.R.color.darker_gray
            }
            itemView.setBackgroundResource(backgroundColor)
            itemView.picture.setImageResource(imageResource)
        }
    }

    interface OnSelectListener {
        fun onSelect(track: Track, selected: Boolean)
    }
}
