package ru.den.musicplayer.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_track_list.*
import org.koin.android.ext.android.inject
import ru.den.musicplayer.R
import ru.den.musicplayer.models.PlaylistTypeManager
import ru.den.musicplayer.ui.adapters.AlbumPagerAdapter

enum class ScrollDirection {
    UP, DOWN
}

/**
 * A simple [Fragment] subclass.
 * Use the [TrackListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TrackListFragment : Fragment() {

    companion object {
        private const val TAG = "TrackListFragment"

        @JvmStatic
        fun newInstance() = TrackListFragment()
        private var bottomPlayerIsVisible = false
    }

    private val playlistTypeManager: PlaylistTypeManager by inject()
    private lateinit var mediaPlayerHost: MediaPlayer

    private lateinit var albumPagerAdapter: AlbumPagerAdapter

    private var mediaCallbacks = object : MediaPlayerCallbacks {
        override fun onStartPlay() {
            updateMiniPlayerAction()
        }

        override fun onPause() {
            updateMiniPlayerAction()
        }

        override fun onStop() {
            updateMiniPlayerAction()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        albumPagerAdapter = AlbumPagerAdapter(this)
        viewPager.adapter = albumPagerAdapter

        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = playlistTypeManager.playlistTypes[position].title
        }.attach()
    }

    override fun onStart() {
        super.onStart()
       // configureBottomMediaPlayer()
        mediaPlayerHost.registerMediaPlayerCallbacks(mediaCallbacks)

        if (bottomPlayerIsVisible) {
           // showBottomMediaPlayerControl()
            //updateProgress(playlist.trackProgress)
            updateMiniPlayerAction()
        }
        tabs.tabMode = TabLayout.MODE_SCROLLABLE
    }

//    private fun updateProgress(progress: Int, max: Int = playlist.currentTrack?.duration ?: 100) {
//        progressBar?.max = max
//        progressBar?.progress = progress
//        elapsedTime?.text = "${Track.formatTrackTime(progressBar.progress)}/${Track.formatTrackTime(progressBar.max)}"
//    }

    override fun onStop() {
        super.onStop()
        mediaPlayerHost.unregisterMediaPlayerCallbacks(mediaCallbacks)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mediaPlayerHost = context as MediaPlayer
    }

    private fun updateMiniPlayerAction() {
//        if (playlistManager.isPlaying) {
//            musicAction?.setImageResource(R.drawable.ic_bottom_pause)
//           // audioFilesAdapter.setActiveTrackIndex(playlist.currentTrackInd)
//        } else {
//            musicAction?.setImageResource(R.drawable.ic_bottom_play)
//           // audioFilesAdapter.setActiveTrackIndex(-1)
//        }
//        trackTitle?.text = playlist.currentTrack?.name
    }

//    private fun configureBottomMediaPlayer() {
//        musicAction.setOnClickListener {
////            if (playlistManager.isPlaying) {
////                pause()
////            } else {
////                play()
////            }
//        }
//
//        next.setOnClickListener {
//            mediaPlayerHost.nextTrack()
//        }
//
//        prev.setOnClickListener {
//            mediaPlayerHost.prevTrack()
//        }
//
//        progressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {
//
//            }
//
//            override fun onStopTrackingTouch(seekBar: SeekBar?) {
//                seekBar?.progress?.let {
//                    mediaPlayerHost.seekTo(it)
//                }
//            }
//        })
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_track_list, container, false)
    }

    private fun play() {
        mediaPlayerHost.play()
        //showBottomMediaPlayerControl()
    }

    private fun pause() {
       // audioFilesAdapter.setActiveTrackIndex(-1)
        mediaPlayerHost.pause()
    }
}
