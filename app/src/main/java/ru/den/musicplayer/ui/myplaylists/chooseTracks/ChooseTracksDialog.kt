package ru.den.musicplayer.ui.myplaylists.chooseTracks

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.android.ext.android.inject
import ru.den.musicplayer.R
import ru.den.musicplayer.databinding.DialogChooseTracksBinding
import ru.den.musicplayer.models.Track
import ru.den.musicplayer.searcher.MusicSearchCriteria
import ru.den.musicplayer.ui.dialogs.FullscreenDialog
import ru.den.musicplayer.utils.MyTextWatcher

class ChooseTracksDialog : FullscreenDialog(), TracksAdapter.OnSelectListener {
    private val viewModel: ChooseTracksViewModel by inject()
    private lateinit var recyclerTracksAdapter: TracksAdapter
    private lateinit var binding: DialogChooseTracksBinding

    companion object {
        const val KEY_SELECTED_ITEMS = "selected_items"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate<DialogChooseTracksBinding>(
            inflater,
            R.layout.dialog_choose_tracks, container, false
        )

        configure()
        arguments?.getStringArray(KEY_SELECTED_ITEMS)?.let { items ->
            viewModel.selectedItems.addAll(items.toList())
            recyclerTracksAdapter.checked.clear()
            recyclerTracksAdapter.checked.addAll(items.toSet())
        }
        viewModel.searchTracks(null)

        return binding.root
    }

    private fun configure() {
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.searchSubject.addTextChangedListener(object : MyTextWatcher() {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                viewModel.searchValue.value = s.toString()
                viewModel.searchTracks(MusicSearchCriteria(title = s.toString()))
            }
        })

        viewModel.isSearch.observe(viewLifecycleOwner, Observer { isSearch ->
            if (isSearch) {
                Log.d("ChooseTracksDialog", "isSearch")
                binding.searchSubject.post(Runnable {
                    binding.searchSubject.isFocusableInTouchMode = true
                    binding.searchSubject.requestFocus()
                    val im = requireActivity().applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    im.showSoftInput(binding.searchSubject, InputMethodManager.SHOW_IMPLICIT)
                })
            }
        })

        viewModel.eventOnSaveTracks.observe(viewLifecycleOwner, Observer { value ->
            if (value) {
                val intent = Intent().apply {
                    putExtra(KEY_SELECTED_ITEMS, viewModel.selectedItems.toList().toTypedArray())
                }
                targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
                viewModel.onSaveTracksFinished()
                dialog?.dismiss()
            }
        })

        viewModel.tracks.observe(viewLifecycleOwner, Observer { tracks ->
            recyclerTracksAdapter.updateItems(tracks)
        })

        recyclerTracksAdapter = TracksAdapter(mutableListOf(), this)
        binding.tracksRecyclerView.adapter = recyclerTracksAdapter
        binding.tracksRecyclerView.layoutManager = LinearLayoutManager(activity)
    }

    override fun onSelect(track: Track, selected: Boolean) {
        if (selected) {
            viewModel.selectedItems.add(track.id)
        } else {
            viewModel.selectedItems.remove(track.id)
        }
        binding.invalidateAll()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(requireActivity(), theme) {
            override fun onBackPressed() {
                if (viewModel.isSearch.value == true) {
                    viewModel.isSearch.value = false
                } else {
                    super.onBackPressed()
                }
            }
        }
    }
}
