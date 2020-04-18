package ru.den.musicplayer.ui

import android.app.Dialog
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

import ru.den.musicplayer.R

class MediaPlayerFragment : BottomSheetDialogFragment() {

    companion object {
        fun newInstance() = MediaPlayerFragment()
        const val TAG = "MediaPlayerFragment"
    }

    private lateinit var viewModel: MediaPlayerViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.media_player_fragment, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MediaPlayerViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
