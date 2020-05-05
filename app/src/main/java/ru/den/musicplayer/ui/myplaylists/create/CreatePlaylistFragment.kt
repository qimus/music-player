package ru.den.musicplayer.ui.myplaylists.create

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil

import ru.den.musicplayer.R
import ru.den.musicplayer.databinding.FragmentCreatePlaylistBinding

/**
 * A simple [Fragment] subclass.
 */
class CreatePlaylistFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentCreatePlaylistBinding>(inflater,
            R.layout.fragment_create_playlist, container, false)

        return binding.root
    }

}
