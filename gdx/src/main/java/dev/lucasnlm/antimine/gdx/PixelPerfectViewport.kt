package dev.lucasnlm.antimine.gdx

import com.badlogic.gdx.utils.viewport.FitViewport
import kotlin.math.floor

class PixelPerfectViewport(
    worldWidth: Float,
    worldHeight: Float,
) : FitViewport(worldWidth, worldHeight) {
    override fun update(
        screenWidth: Int,
        screenHeight: Int,
        centerCamera: Boolean,
    ) {
        val wRate = screenWidth / worldWidth
        val hRate = screenHeight / worldHeight
        val rate = wRate.coerceAtMost(hRate)

        val iRate = 1f.coerceAtLeast(floor(rate))

        val viewportWidth = worldWidth.toInt() * iRate
        val viewportHeight = worldHeight.toInt() * iRate

        setScreenBounds(
            ((screenWidth - viewportWidth) * CENTER_FACTOR).toInt(),
            ((screenHeight - viewportHeight) * CENTER_FACTOR).toInt(),
            viewportWidth.toInt(),
            viewportHeight.toInt(),
        )
        apply(false)
    }

    private companion object {
        const val CENTER_FACTOR = 0.5f
    }
}
