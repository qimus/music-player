package ru.den.musicplayer.ui.dialogs

import androidx.fragment.app.DialogFragment
import ru.den.musicplayer.R

open class FullscreenDialog : DialogFragment() {
    override fun getTheme(): Int {
        return R.style.DialogTheme
    }
}
