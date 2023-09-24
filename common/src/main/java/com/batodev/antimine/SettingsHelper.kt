package com.batodev.antimine
import android.content.Context
import android.content.SharedPreferences
import android.os.Build

class SettingsHelper(context: Context) {
    private val sharedPreferences: SharedPreferences
    val preferences: Preferences

    init {
        sharedPreferences = context.getSharedPreferences("SettingsHelper", Context.MODE_PRIVATE)
        preferences = loadPreferences()
    }

    fun savePreferences() {
        val editor = sharedPreferences.edit()
        editor.putString("uncoveredPics", preferences.uncoveredPics.joinToString(","))
        editor.putInt("lastSeenGalleryPic", preferences.lastSeenGalleryPic)
        editor.apply()
    }

    private fun loadPreferences(): Preferences {
        val preferences = Preferences(mutableListOf())
        preferences.uncoveredPics =
            sharedPreferences.getString("uncoveredPics", "")?.split(",")?.toMutableList()
                ?: mutableListOf()
        preferences.uncoveredPics = preferences.uncoveredPics.filter { it != "" }.toMutableList()
        preferences.lastSeenGalleryPic = sharedPreferences.getInt("lastSeenGalleryPic", 0)
        return preferences
    }
}

data class Preferences(
    var uncoveredPics: MutableList<String>,
    var lastSeenGalleryPic: Int = 0
)