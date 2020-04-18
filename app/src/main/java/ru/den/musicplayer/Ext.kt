package ru.den.musicplayer

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

fun Context.convertDpToPx(dp: Float): Float {
    return dp * (resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
}

fun Context.convertPxInDp(pixels: Float): Float {
    return pixels / (resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
}

inline fun <reified T> Context.createIntent(): Intent {
    return Intent(this, T::class.java)
}

inline fun <reified T: Activity> Activity.startActivity() {
    startActivity(createIntent<T>())
}
