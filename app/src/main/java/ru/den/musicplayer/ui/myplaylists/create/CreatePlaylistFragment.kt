package ru.den.musicplayer.ui.myplaylists.create

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.koin.android.ext.android.inject

import ru.den.musicplayer.R
import ru.den.musicplayer.databinding.FragmentCreatePlaylistBinding
import ru.den.musicplayer.ui.myplaylists.chooseTracks.ChooseTracksDialog

const val CHOOSE_TRACKS_REQUEST =1

/**
 * A simple [Fragment] subclass.
 */
class CreatePlaylistFragment : Fragment() {
    companion object {
        fun newInstance() = CreatePlaylistFragment()
    }

    private val viewModel: PlaylistEditViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentCreatePlaylistBinding>(inflater,
            R.layout.fragment_create_playlist, container, false)

        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.new_playlist_title)

        binding.playlistName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.name.value = s.toString()
            }
        })

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.addTracks.setOnClickListener { launchSetTracksFragment() }

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i("CreatePlaylistFragment", "onActivityResult")
    }

    private fun launchSetTracksFragment() {
        val chooseTracksFragment =
            ChooseTracksDialog()
        chooseTracksFragment.setTargetFragment(this, CHOOSE_TRACKS_REQUEST)
        chooseTracksFragment.show(requireActivity().supportFragmentManager,
            chooseTracksFragment::class.simpleName)
    }
}
