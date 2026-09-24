package com.batodev.antimine

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.card.MaterialCardView
import dev.lucasnlm.external.AdsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class GalleryActivity : Activity() {
    val pics = mutableListOf<String>()
    var currentPic = ""
    var scrollCount = 0
    private val adsManager: AdsManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.gallery_activity)
        avoidNavigationBarOverlap(findViewById<MaterialCardView>(R.id.gallery_bottom_bar))
    }

    /**
     * Keeps a bottom-pinned control clear of the strip along the bottom of the screen where
     * the system takes touches itself, or it renders there and becomes untappable.
     *
     * Which inset describes that strip depends on the screen: with the system bars visible the
     * nav bar window sits over the app (navigationBars()); with them hidden, navigationBars()
     * reports 0 but the same band stays reserved for system gestures (systemGestures()).
     * Padding by the larger of the two covers both cases.
     */
    private fun avoidNavigationBarOverlap(view: View) {
        val baseBottomMargin = (view.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val navBarBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            val gestureBottom = insets.getInsets(WindowInsetsCompat.Type.systemGestures()).bottom
            (v.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin =
                baseBottomMargin + maxOf(navBarBottom, gestureBottom)
            v.requestLayout()
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        pics.clear()
        val settingsHelper = SettingsHelper(this)
        pics.addAll(settingsHelper.preferences.uncoveredPics)
        if (!pics.isEmpty()) {
            findViewById<PhotoView>(R.id.gallery_activity_background).setImageBitmap(
                ImageHelper.findBitmap(pics[settingsHelper.preferences.lastSeenGalleryPic], this),
            )
            currentPic = pics[settingsHelper.preferences.lastSeenGalleryPic]
        }
    }

    fun leftClicked(ignored: View) {
        val indexOf = pics.indexOf(currentPic)
        if (indexOf > 0) {
            findViewById<PhotoView>(R.id.gallery_activity_background).setImageBitmap(
                ImageHelper.findBitmap(pics[indexOf - 1], this),
            )
            currentPic = pics[indexOf - 1]
            saveLastSeenPic(indexOf - 1)
        }
        showAD()
    }

    fun rightClicked(ignored: View) {
        val indexOf = pics.indexOf(currentPic)
        if (indexOf < pics.size - 1) {
            findViewById<PhotoView>(R.id.gallery_activity_background).setImageBitmap(
                ImageHelper.findBitmap(pics[indexOf + 1], this),
            )
            currentPic = pics[indexOf + 1]
            saveLastSeenPic(indexOf + 1)
        }
        showAD()
    }

    private fun saveLastSeenPic(indexOf: Int) {
        val scope = CoroutineScope(Dispatchers.IO)
        val settingsHelper = SettingsHelper(this)
        scope.launch {
            val result =
                async {
                    settingsHelper.preferences.lastSeenGalleryPic = indexOf
                    settingsHelper.savePreferences()
                }
            val data = result.await()
            Log.d(GalleryActivity::class.java.simpleName, "$data")
        }
    }

    private fun showAD() {
        Log.d(GalleryActivity::class.java.simpleName, "Showing ad.")
        if (++scrollCount >= AD_SCROLL_THRESHOLD) {
            adsManager.showInterstitialAd(this, onDismiss = {})
            scrollCount = 0
        }
    }

    fun backClicked(ignored: View) {
        finish()
    }

    fun shareClicked(ignored: View) {
        if (currentPic != "") {
            val imgFolder = PRIZE_IMAGES
            val inputStream = assets.open("${imgFolder}${File.separator}$currentPic")
            val tmpImgPath = "tmp_shared/tmp.png"
            val file = File(filesDir, tmpImgPath)
            File(filesDir, "tmp_shared").mkdirs()
            file.delete()
            val outputStream: OutputStream = FileOutputStream(file)
            val buffer = ByteArray(COPY_BUFFER_SIZE)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            inputStream.close()
            outputStream.close()
            val shareIntent = Intent(Intent.ACTION_SEND)
            val uri = "content://com.batodev.antimine.ImagesProvider/$tmpImgPath".toUri()
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent.clipData = android.content.ClipData.newRawUri("", uri)
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            shareIntent.type = "image/*"
            startActivity(shareIntent)
        }
    }

    private companion object {
        const val AD_SCROLL_THRESHOLD = 3
        const val COPY_BUFFER_SIZE = 1024
    }
}
