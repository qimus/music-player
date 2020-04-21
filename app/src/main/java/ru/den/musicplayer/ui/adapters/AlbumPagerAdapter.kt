package ru.den.musicplayer.ui.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.koin.core.KoinComponent
import org.koin.core.inject
import ru.den.musicplayer.models.PlaylistTypeManager

class AlbumPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment), KoinComponent {
    private val playlistTypeManager: PlaylistTypeManager by inject()

    override fun getItemCount(): Int = playlistTypeManager.playlistTypes.size

    override fun createFragment(position: Int): Fragment {
        return playlistTypeManager.playlistTypes[position].createFragment()
    }
}
