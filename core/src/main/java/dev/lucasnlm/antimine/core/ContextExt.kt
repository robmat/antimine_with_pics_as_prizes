package dev.lucasnlm.antimine.core

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.core.net.toUri

fun Context.isPortrait(): Boolean = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

fun Context.dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

/**
 * Opens [url] in an external app (e.g. a browser), calling [onError] if it fails
 * (e.g. no app can handle the intent). [beforeLaunch] runs right before the intent
 * is launched and is covered by the same failure handling.
 */
fun Context.openExternalLink(
    url: String,
    beforeLaunch: () -> Unit = {},
    onError: () -> Unit = {},
) {
    runCatching {
        beforeLaunch()
        val intent =
            Intent(Intent.ACTION_VIEW, url.toUri()).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        startActivity(intent)
    }.onFailure {
        onError()
    }
}
