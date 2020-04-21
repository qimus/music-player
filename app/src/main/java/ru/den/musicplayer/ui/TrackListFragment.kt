package ru.den.musicplayer.ui

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_track_list.*
import kotlinx.android.synthetic.main.fragment_track_list.next
import kotlinx.android.synthetic.main.fragment_track_list.prev
import kotlinx.android.synthetic.main.fragment_track_list.progressBar
import kotlinx.android.synthetic.main.fragment_track_list.trackTitle
import org.koin.android.ext.android.inject
import ru.den.musicplayer.R
import ru.den.musicplayer.convertDpToPx
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
        private const val MIN_PLAYER_HEIGHT = 70f

        @JvmStatic
        fun newInstance() = TrackListFragment()
        private var bottomPlayerIsVisible = false
    }

    private val playlistTypeManager: PlaylistTypeManager by inject()
    //private val playlist = playlistManager.currentPlaylist
    //private val audioFilesAdapter = TrackListAdapter(mutableListOf(), this)
    private lateinit var mediaPlayerHost: MediaPlayer
    private var currentPlayerHeight = 0
    private var maxViewHeightPx = 0
    private var minPlayerHeightPx = 0
    private var lastScrollDirection: ScrollDirection = ScrollDirection.UP

    private lateinit var albumPagerAdapter: AlbumPagerAdapter

    private var gestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            val offset = e1.y - e2.y + currentPlayerHeight
            lastScrollDirection = if (e1.y > e2.y) {
                ScrollDirection.UP
            } else {
                ScrollDirection.DOWN
            }

            if (maxViewHeightPx - e2.y < minPlayerHeightPx || offset > maxViewHeightPx + 50) {
                return true
            }
            val lp = miniPlayerLayout.layoutParams
            lp.height = offset.toInt()
            miniPlayerLayout.layoutParams = lp
            return true
        }

        override fun onDown(e: MotionEvent?): Boolean {
            currentPlayerHeight = miniPlayerLayout.layoutParams.height
            return true
        }
    }

    private lateinit var gestureDetector: GestureDetector

    private var mediaCallbacks = object : MediaPlayerCallbacks {
        override fun onStartPlay() {
            updateMiniPlayerAction()
        }

        override fun onPause() {
            updateMiniPlayerAction()
        }

        override fun onPlaying(progress: Int) {
//            playlist.currentTrack?.let {
//                updateProgress(progress)
//            }
        }

        override fun onStop() {
            updateMiniPlayerAction()
        }

        override fun onNextTrack() {

        }

        override fun onPrevTrack() {

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.post(Runnable {
            maxViewHeightPx = view.height + 50
        })

        context?.let {
            minPlayerHeightPx = it.convertDpToPx(MIN_PLAYER_HEIGHT).toInt() + 50
        }

        gestureDetector = GestureDetector(context, gestureListener)
        view.setOnTouchListener { v, event ->
            if (gestureDetector.onTouchEvent(event)) {
                true
            } else {
                when(event.action) {
                    MotionEvent.ACTION_UP -> {
                        if (lastScrollDirection == ScrollDirection.UP) {
                            miniPlayerRollUp(miniPlayerLayout.layoutParams.height, maxViewHeightPx)
                        } else {
                            miniPlayerRollUp(miniPlayerLayout.layoutParams.height, minPlayerHeightPx)
                        }
                    }
                }
                true
            }
        }

        val argbEvaluator = ArgbEvaluator()
        miniPlayerLayout.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            val scrollUpPercent = (v.layoutParams.height.toFloat() - minPlayerHeightPx) / view.height
            val backgroundColor = argbEvaluator.evaluate(scrollUpPercent, 0xFF0F1E36.toInt(), 0xFFFFFFFF.toInt()) as Int
            miniPlayer.setBackgroundColor(backgroundColor)
            var alpha = 1 - scrollUpPercent
            if (scrollUpPercent > 0.2) {
                alpha -= 0.5f
            }
            updateMiniPlayerVisible(alpha)
        }

        albumPagerAdapter = AlbumPagerAdapter(this)
        viewPager.adapter = albumPagerAdapter

        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = playlistTypeManager.playlistTypes[position].title
        }.attach()
    }

    private fun updateMiniPlayerVisible(alpha: Float) {
        progressBar.alpha = alpha
        prev.alpha = alpha
        next.alpha = alpha
        musicAction.alpha = alpha
        trackTitle.alpha = alpha
        elapsedTime.alpha = alpha
    }

    private fun miniPlayerRollUp(start: Int, end: Int) {
        val animator = ValueAnimator.ofInt(start, end).apply {
            interpolator = DecelerateInterpolator()
            duration = 200
            start()
        }

        animator.addUpdateListener {
            val animatedValue = it.animatedValue as Int
            miniPlayerLayout.layoutParams.height = animatedValue
            miniPlayerLayout.requestLayout()
        }
    }

    override fun onStart() {
        super.onStart()
        configureBottomMediaPlayer()
        mediaPlayerHost.registerMediaPlayerCallbacks(mediaCallbacks)

        if (bottomPlayerIsVisible) {
            showBottomMediaPlayerControl()
            //updateProgress(playlist.trackProgress)
            updateMiniPlayerAction()
        }
        tabs.tabMode = TabLayout.MODE_SCROLLABLE

        progressBar.setPadding(0, 0, 0, 0)
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

    private fun configureBottomMediaPlayer() {
        musicAction.setOnClickListener {
//            if (playlistManager.isPlaying) {
//                pause()
//            } else {
//                play()
//            }
        }

        next.setOnClickListener {
            mediaPlayerHost.nextTrack()
        }

        prev.setOnClickListener {
            mediaPlayerHost.prevTrack()
        }

        progressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.progress?.let {
                    mediaPlayerHost.seekTo(it)
                }
            }
        })
    }

    private fun showBottomMediaPlayerControl() {
        val animator = ValueAnimator.ofInt(miniPlayerLayout.layoutParams.height, 250).apply {
            duration = 300
            interpolator = LinearInterpolator()
            start()
        }

        miniPlayerLayout.visibility = View.VISIBLE

        animator.addUpdateListener {
            val value = it.animatedValue as Int
            miniPlayerLayout.layoutParams.height = value
            miniPlayerLayout.requestLayout()
        }

        bottomPlayerIsVisible = true
    }

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
