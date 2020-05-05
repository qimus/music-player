package ru.den.musicplayer.ui.myplaylists

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import org.koin.android.ext.android.inject

import ru.den.musicplayer.R

/**
 * A simple [Fragment] subclass.
 */
class PlaylistFragment : Fragment() {

    private val viewModel: PlaylistsViewModel by inject()
    private lateinit var adapter: PlaylistAdapter

    companion object {
        @JvmStatic
        fun newInstance() = PlaylistFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_playlist, container, false)

        adapter = PlaylistAdapter()
        viewModel.playlists.observe(viewLifecycleOwner, Observer { playlists ->
            adapter.updateItems(playlists)
        })

        return view
    }
}
