package ru.den.musicplayer.ui.myplaylists.chooseTracks

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.android.ext.android.inject
import ru.den.musicplayer.R
import ru.den.musicplayer.databinding.DialogChooseTracksBinding
import ru.den.musicplayer.searcher.MusicSearchCriteria
import ru.den.musicplayer.ui.dialogs.FullscreenDialog

class ChooseTracksDialog : FullscreenDialog() {
    private val viewModel: ChooseTracksViewModel by inject()
    private lateinit var recyclerTracksAdapter: TracksAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<DialogChooseTracksBinding>(
            inflater,
            R.layout.dialog_choose_tracks, container, false
        )

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        configure(binding)
        viewModel.searchTracks(null)

        return binding.root
    }

    private fun configure(binding: DialogChooseTracksBinding) {
        binding.searchSubject.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchValue.value = s.toString()
                viewModel.searchTracks(MusicSearchCriteria(title = s.toString()))
            }
        })

        viewModel.isSearch.observe(viewLifecycleOwner, Observer { isSearch ->
            if (isSearch) {
                binding.searchSubject.requestFocus()
            }
        })

        viewModel.tracks.observe(viewLifecycleOwner, Observer { tracks ->
            recyclerTracksAdapter.updateItems(tracks)
        })

        recyclerTracksAdapter = TracksAdapter()
        binding.tracksRecyclerView.adapter = recyclerTracksAdapter
        binding.tracksRecyclerView.layoutManager = LinearLayoutManager(activity)
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
