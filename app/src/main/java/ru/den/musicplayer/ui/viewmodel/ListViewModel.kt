package ru.den.musicplayer.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.den.musicplayer.searcher.Searcher

class ListViewModel<R, C, S: Searcher<C?, List<R>>>(private val searcher: S) : ViewModel() {
    private val items = MutableLiveData<List<R>>()

    fun getItems(): LiveData<List<R>> = items

    fun search(criteria: C?) {
        items.value = searcher.search(criteria)
        Log.d("ListViewModel", "criteria: $criteria, items: ${searcher.search(criteria)}")
    }
}
