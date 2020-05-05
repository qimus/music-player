package ru.den.musicplayer.ui

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_player.*
import org.koin.android.ext.android.inject
import ru.den.musicplayer.R
import ru.den.musicplayer.convertDpToPx
import ru.den.musicplayer.models.CurrentPlaylist
import ru.den.musicplayer.models.Track

enum class PlayerSlideState {
    OPENED, COLLAPSED, INVISIBLE
}

/**
 * A simple [Fragment] subclass.
 * Use the [PlayerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PlayerFragment : Fragment(), BackPressedBehavior {

    companion object {
        private const val TAG = "PlayerFragment"
        private const val PLAYER_MIN_HEIGHT = 85f

        @JvmStatic
        fun newInstance() = PlayerFragment()
    }

    private val currentPlaylist: CurrentPlaylist by inject()

    private lateinit var mediaPlayer: MediaPlayer
    private var currentPlayerHeight = 0
    private var lastScrollDirection: ScrollDirection = ScrollDirection.UP
    private var maxViewHeightPx = 0
    private var layoutHeight = 0
    private var minPlayerHeightPx = 0
    private lateinit var gestureDetector: GestureDetector
    private var playerState = PlayerSlideState.INVISIBLE

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

            if (layoutHeight - e2.y < minPlayerHeightPx || offset > maxViewHeightPx + 50) {
                return true
            }
            //Log.d(TAG, "${e1.y} ${e2.y} ${offset}")
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

    private val mediaPlayerCallbacks = object : MediaPlayerCallbacks {
        override fun onStartPlay() {
            super.onStartPlay()
            updateControlsState()
            if (currentPlaylist.isPlaying && playerState == PlayerSlideState.INVISIBLE) {
                showBottomMediaPlayerControl()
                playerState = PlayerSlideState.COLLAPSED
            }
        }

        override fun onPause() {
            super.onPause()
            updateControlsState()
        }

        override fun onPlaying(progress: Int) {
            super.onPlaying(progress)
            updateProgress(progress)
        }

        override fun onStop() {
            super.onStop()
            updateControlsState()
        }

        override fun onNextTrack() {
            super.onNextTrack()
            updateTrackName()
        }

        override fun onPrevTrack() {
            super.onPrevTrack()
            updateTrackName()
        }
    }

    override fun onBackPressed(): Boolean {
        if (playerState == PlayerSlideState.OPENED) {
            collapsePlayer()
            return true
        }

        return false
    }

    private fun updateProgress(progress: Int, max: Int = currentPlaylist.currentTrack?.duration ?: 100) {
        progressBar.max = max
        progressBar.progress = progress
        elapsedTime.text =
            "${Track.formatTrackTime(progress)}/${Track.formatTrackTime(max)}"

        progressBar2.max = max
        progressBar2.progress = progress

        playedTime.text = Track.formatTrackTime(progressBar.progress)
        estimateTime.text = Track.formatTrackTime(max)
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
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mediaPlayer = context as MediaPlayer
    }

    override fun onResume() {
        super.onResume()
        mediaPlayer.registerMediaPlayerCallbacks(mediaPlayerCallbacks)
        if (currentPlaylist.isPlaying && playerState == PlayerSlideState.INVISIBLE) {
            showBottomMediaPlayerControl()
            playerState = PlayerSlideState.COLLAPSED
        }
        updateTrackName()
        updateControlsState()
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer.unregisterMediaPlayerCallbacks(mediaPlayerCallbacks)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_player, container, false)
        minPlayerHeightPx = view.context.convertDpToPx(PLAYER_MIN_HEIGHT).toInt()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gestureDetector = GestureDetector(view.context, gestureListener)
        trackTitle.isSelected = true

        val rootView = view.rootView as ViewGroup
        val mainLayout = rootView.findViewById<ConstraintLayout>(R.id.mainLayout)

        view.post {
            maxViewHeightPx = mainLayout.height
            layoutHeight = rootView.height
        }
        rootView.setOnTouchListener { v, event ->
            if (gestureDetector.onTouchEvent(event)) {
                true
            } else {
                when (event.action) {
                    MotionEvent.ACTION_UP -> {
                        if (lastScrollDirection == ScrollDirection.UP) {
                            showFullPlayer()
                        } else {
                            if (miniPlayerLayout.height.toFloat() / maxViewHeightPx > 0.6) {
                                showFullPlayer()
                            } else {
                                collapsePlayer()
                            }
                        }
                    }
                }
                true
            }
        }

        val argbEvaluator = ArgbEvaluator()
        miniPlayerLayout.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            var scrollUpPercent = if (maxViewHeightPx > 0)
                (v.layoutParams.height.toFloat() - minPlayerHeightPx) / maxViewHeightPx
            else
                0f

            if (scrollUpPercent > 0.6) {
                scrollUpPercent = 1f
            }

            val backgroundColor = argbEvaluator.evaluate(
                scrollUpPercent,
                0xFF0F1E36.toInt(),
                0xFFFFFFFF.toInt()
            ) as Int
            miniPlayer.setBackgroundColor(backgroundColor)
            // Log.d(TAG, "scrollUpPercent (${v.layoutParams.height} - $minPlayerHeightPx) / $maxViewHeightPx = $scrollUpPercent")
            var alpha = 1 - scrollUpPercent
            if (scrollUpPercent > 0.2) {
                alpha -= 0.5f
            }
            updateMiniPlayerVisible(alpha)
        }

        configureMediaPlayer()
    }

    private fun showFullPlayer() {
        miniPlayerRollUp(
            miniPlayerLayout.layoutParams.height,
            maxViewHeightPx
        )
        playerState = PlayerSlideState.OPENED
    }

    private fun collapsePlayer() {
        miniPlayerRollUp(
            miniPlayerLayout.layoutParams.height,
            minPlayerHeightPx
        )
        playerState = PlayerSlideState.COLLAPSED
    }

    private fun configureMediaPlayer() {
        val onPlayListener: View.OnClickListener = View.OnClickListener {
            if (currentPlaylist.isPlaying) {
                mediaPlayer.pause()
            } else {
                mediaPlayer.play()
            }
        }

        musicAction.setOnClickListener(onPlayListener)
        play2.setOnClickListener(onPlayListener)

        val nextTrackListener = View.OnClickListener { mediaPlayer.nextTrack() }
        val prevTrackListener = View.OnClickListener { mediaPlayer.prevTrack() }
        next.setOnClickListener(nextTrackListener)
        next2.setOnClickListener(nextTrackListener)
        prev.setOnClickListener(prevTrackListener)
        prev2.setOnClickListener(prevTrackListener)

        progressBar.setPadding(0, 0, 0, 0)

        val onSeekChangeListener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                mediaPlayer.seekTo(seekBar.progress)
            }
        }

        progressBar.setOnSeekBarChangeListener(onSeekChangeListener)
        progressBar2.setOnSeekBarChangeListener(onSeekChangeListener)
    }

    private fun updateControlsState() {
        if (currentPlaylist.isPlaying) {
            musicAction.setImageResource(R.drawable.ic_bottom_pause)
            play2.setImageResource(R.drawable.ic_bottom_pause)
        } else {
            musicAction.setImageResource(R.drawable.ic_bottom_play)
            play2.setImageResource(R.drawable.ic_player_play)
        }

        updateTrackName()
    }

    private fun updateTrackName() {
        trackTitle.text = currentPlaylist.currentTrack?.name
        trackTitle2.text = currentPlaylist.currentTrack?.name
        trackAlbum.text = currentPlaylist.currentTrack?.album
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
}
