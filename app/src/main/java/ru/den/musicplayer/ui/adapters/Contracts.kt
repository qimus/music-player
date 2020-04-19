package ru.den.musicplayer.ui.adapters

interface Contract {
    interface Adapter<M> {
        fun updateItems(items: List<M>)
        fun setOnItemSelectListener(onItemSelectListener: OnItemSelectContract<M>)
    }

    interface OnItemSelectContract<M> {
        fun onSelect(model: M)
    }
}
