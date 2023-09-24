package com.batodev.antimine

import android.content.Context
import android.util.Log

const val PRIZE_IMAGES = "prize-images"

object ImageHelper {
    fun randomImage(context: Context) : String {
        val allImages = context.assets.list(PRIZE_IMAGES)!!.toMutableList()
        val uncoveredPics = SettingsHelper(context).preferences.uncoveredPics
        allImages.removeAll(uncoveredPics)
        val imageDrawn = if (allImages.isEmpty()) {
            uncoveredPics.random()
        } else {
            allImages.random()
        }
        Log.d(ImageHelper::class.java.simpleName, "imageDrawn: $imageDrawn")
        return imageDrawn
    }
}