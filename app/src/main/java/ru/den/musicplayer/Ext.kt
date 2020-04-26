package ru.den.musicplayer

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.app.NotificationCompat
import androidx.media.session.MediaButtonReceiver

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

fun MediaSessionCompat.createNotificationBuilder(context: Context, channelId: String): NotificationCompat.Builder {
    val mediaMetadata = controller.metadata
    val description = mediaMetadata.description

    return NotificationCompat.Builder(context, channelId).apply {
        setContentTitle(description.title)
        setContentText(description.subtitle)
        setSubText(description.description)
        setLargeIcon(description.iconBitmap)
        setContentIntent(controller.sessionActivity)
        setDeleteIntent(
            MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP)
        )
        setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
    }
}
