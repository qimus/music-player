package ru.den.musicplayer.ui.adapters

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.list_item.view.*
import ru.den.musicplayer.R
import ru.den.musicplayer.inflate
import ru.den.musicplayer.models.Album
import ru.den.musicplayer.models.Artist
import ru.den.musicplayer.models.SoundEntity
import ru.den.musicplayer.models.Year

class ListAdapter<M : SoundEntity>(private val items: MutableList<M> = mutableListOf())
    : RecyclerView.Adapter<ListAdapter<M>.ListViewHolder>(), Contract.Adapter<M> {

    private lateinit var onSelectListener: Contract.OnSelectItem<M>

    override fun updateItems(items: List<M>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun setOnItemSelectListener(onSelectListener: Contract.OnSelectItem<M>) {
        this.onSelectListener = onSelectListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        return ListViewHolder(parent.inflate(R.layout.list_item, false))
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val album = items[position]
        holder.bind(album)
    }

    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: M) {
            itemView.setOnClickListener {
                onSelectListener.onSelect(item)
            }

            when (item::class) {
                Album::class -> {
                    itemView.name.text = (item as Album).name
                }
                Artist::class -> {
                    itemView.name.text = (item as Artist).name
                }
                Year::class -> {
                    Log.d("ListViewHolder", (item as Year).year)
                    itemView.name.text = (item as Year).year
                }
            }

        }
    }
}
