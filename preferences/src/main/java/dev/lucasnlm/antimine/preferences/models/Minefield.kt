package dev.lucasnlm.antimine.preferences.models

import androidx.annotation.Keep

@Keep
data class Minefield(
    val width: Int,
    val height: Int,
    val mines: Int,
    val seed: Long? = null,
) {
    private fun ratio(): Double = mines.toDouble() / (width * height)

    fun ratioPercent(): Int = (ratio() * PERCENT_MULTIPLIER).toInt()

    private companion object {
        const val PERCENT_MULTIPLIER = 100.0
    }
}
