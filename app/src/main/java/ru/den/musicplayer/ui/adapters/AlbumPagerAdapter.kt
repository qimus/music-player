package ru.den.musicplayer.ui.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.koin.core.KoinComponent
import org.koin.core.inject
import ru.den.musicplayer.models.PlaylistsManager

class AlbumPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment), KoinComponent {
    private val playlistsManager: PlaylistsManager by inject()

    override fun getItemCount(): Int = playlistsManager.playlistTypes.size

    override fun createFragment(position: Int): Fragment {
        return playlistsManager.playlistTypes[position].createFragment()
    }
}
