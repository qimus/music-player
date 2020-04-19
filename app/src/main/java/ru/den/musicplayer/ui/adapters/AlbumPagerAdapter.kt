package ru.den.musicplayer.ui.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.koin.core.KoinComponent
import org.koin.core.inject
import ru.den.musicplayer.models.PlaylistManager

class AlbumPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment), KoinComponent {
    private val playlistManager: PlaylistManager by inject()

    override fun getItemCount(): Int = playlistManager.playlists.size

    override fun createFragment(position: Int): Fragment {
        return playlistManager.playlists[position].createFragment()
    }
}
