package ru.den.musicplayer.ui.adapters

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.track_item.view.*
import ru.den.musicplayer.R
import ru.den.musicplayer.inflate
import ru.den.musicplayer.models.Track

class TrackListAdapter(
    private var tracks: List<Track>,
    private var onSelectListener: OnTrackListener
) :
    RecyclerView.Adapter<TrackListAdapter.TrackListViewHolder>()
{
    companion object {
        private const val TAG = "AudioFileAdapter"
    }

    private var playingTrackId: String = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackListViewHolder {
        return TrackListViewHolder(parent.inflate(R.layout.track_item, false))
    }

    override fun getItemCount() = tracks.size

    override fun onBindViewHolder(holder: TrackListViewHolder, position: Int) {
        val track = tracks[position]
        holder.bind(track, position, track.id == playingTrackId)
    }

    fun setActiveTrackId(ind: String) {
        playingTrackId = ind
        notifyDataSetChanged()
    }

    fun updateItems(items: List<Track>) {
        this.tracks = items;
        notifyDataSetChanged()
    }

    inner class TrackListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private lateinit var track: Track
        private var trackInd: Int = 0
        private var isActive = false

        init {
            itemView.setOnClickListener {
                onSelectListener.onTrackSelected(trackInd)
            }
        }

        fun bind(track: Track, trackInd: Int, isActive: Boolean) {
            this.track = track
            this.trackInd = trackInd
            this.isActive = isActive
            itemView.name.text = track.name
            itemView.album.text = track.artist
            itemView.duration.text = track.getFormattedDuration()
            itemView.picture.setImageResource(R.drawable.ic_music_note)

            val resources = itemView.resources
            if (isActive) {
                itemView.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
            } else {
                itemView.setBackgroundColor(resources.getColor(android.R.color.white))
            }
        }
    }

    interface OnTrackListener {
        fun onTrackSelected(trackIndex: Int)
    }
}
