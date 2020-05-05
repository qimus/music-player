package ru.den.musicplayer.ui.myplaylists

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_playlist.view.*
import org.koin.android.ext.android.inject

import ru.den.musicplayer.R
import ru.den.musicplayer.ui.myplaylists.create.CreatePlaylistFragment

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

        view.fab.setOnClickListener {
            activity?.supportFragmentManager?.let {
                it.beginTransaction()
                    .replace(R.id.fragmentContainer, CreatePlaylistFragment.newInstance())
                    .addToBackStack(null)
                    .commit()
            }
        }

        return view
    }
}
