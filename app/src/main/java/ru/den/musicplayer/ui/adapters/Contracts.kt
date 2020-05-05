package ru.den.musicplayer.ui.adapters

interface Contract {
    interface Adapter<M> {
        fun updateItems(items: List<M>)
        fun setOnItemSelectListener(onSelectListener: OnSelectItem<M>)
    }

    interface OnSelectItem<M> {
        fun onSelect(model: M)
    }
}
