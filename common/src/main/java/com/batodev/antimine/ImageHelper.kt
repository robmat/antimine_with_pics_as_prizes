package com.batodev.antimine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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

    fun findBitmap(image: String, context: Context): Bitmap {
        return BitmapFactory.decodeStream(context.assets.open("$PRIZE_IMAGES/$image"))
    }
}