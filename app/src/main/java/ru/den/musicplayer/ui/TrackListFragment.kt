package ru.den.musicplayer.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_track_list.*
import org.koin.android.ext.android.inject
import ru.den.musicplayer.R
import ru.den.musicplayer.models.PlaylistsManager
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
    }

    private val playlistsManager: PlaylistsManager by inject()
    private lateinit var mediaPlayerHost: MediaPlayer

    private lateinit var albumPagerAdapter: AlbumPagerAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        albumPagerAdapter = AlbumPagerAdapter(this)
        viewPager.adapter = albumPagerAdapter
        (activity as AppCompatActivity).supportActionBar?.title = "YAMPlayer"

        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = playlistsManager.playlistTypes[position].title
        }.attach()
    }

    override fun onStart() {
        super.onStart()
        tabs.tabMode = TabLayout.MODE_SCROLLABLE
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mediaPlayerHost = context as MediaPlayer
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_track_list, container, false)
    }
}
