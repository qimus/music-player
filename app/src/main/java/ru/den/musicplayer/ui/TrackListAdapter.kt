package ru.den.musicplayer.ui

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.track_item.view.*
import ru.den.musicplayer.R
import ru.den.musicplayer.inflate
import ru.den.musicplayer.models.Track

class TrackListAdapter(
    private var items: List<Track>,
    private var onSelectListener: OnTrackListener) :
    RecyclerView.Adapter<TrackListAdapter.TrackListViewHolder>()
{
    companion object {
        private const val TAG = "AudioFileAdapter"
    }

    private var playingTrackIndex: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackListViewHolder {
        return TrackListViewHolder(parent.inflate(R.layout.track_item, false))
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: TrackListViewHolder, position: Int) {
        val audioFile = items[position]
        holder.bind(audioFile, position, position == playingTrackIndex)
    }

    fun setPlayingTrackIndex(ind: Int) {
        val previousId = playingTrackIndex
        playingTrackIndex = ind
        notifyItemChanged(ind)
        if (previousId > 0) {
            notifyItemChanged(previousId)
        }
    }

    fun updateItems(items: List<Track>) {
        this.items = items;
        notifyDataSetChanged()
    }

    inner class TrackListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private lateinit var track: Track
        private var itemIndex: Int = 0
        private var isPlaying = false

        init {
            itemView.play.setOnClickListener(View.OnClickListener {
                if (isPlaying) {
                    onSelectListener.pauseTrack(itemIndex)
                } else {
                    onSelectListener.playTrack(itemIndex)
                }
            })
        }

        fun bind(track: Track, position: Int, isPlaying: Boolean) {
            this.track = track
            itemIndex = position
            itemView.name.text = track.name
            itemView.album.text = track.artist
            itemView.duration.text = track.getFormattedDuration()

            this.isPlaying = isPlaying

            if (isPlaying) {
                itemView.play.setImageResource(android.R.drawable.ic_media_pause)
            } else {
                itemView.play.setImageResource(android.R.drawable.ic_media_play)
            }
        }
    }

    interface OnTrackListener {
        fun playTrack(trackId: Int)
        fun pauseTrack(trackId: Int)
    }
}
