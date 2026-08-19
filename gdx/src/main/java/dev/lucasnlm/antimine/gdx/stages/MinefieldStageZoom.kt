package dev.lucasnlm.antimine.gdx.stages

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import dev.lucasnlm.antimine.gdx.GameContext

/**
 * [MinefieldStage]'s zoom-control logic, split out of the class body - see
 * its class doc.
 */
fun MinefieldStage.setZoom(value: Float) {
    (camera as OrthographicCamera).apply {
        zoom = value.coerceIn(MinefieldStage.SET_ZOOM_MIN, MinefieldStage.MAX_ZOOM_IN)
        currentZoom = zoom
        update(true)

        GameContext.zoomLevelAlpha = zoomLevelAlphaFor(zoom)
    }

    inputEvents.clear()
}

fun MinefieldStage.scaleZoom(zoomMultiplier: Float) {
    (camera as OrthographicCamera).apply {
        val newZoom =
            if (zoomMultiplier > 1.0) {
                zoom + 1.0f * Gdx.graphics.deltaTime
            } else {
                zoom - 1.0f * Gdx.graphics.deltaTime
            }
        zoom = newZoom.coerceIn(MinefieldStage.MAX_ZOOM_OUT, MinefieldStage.MAX_ZOOM_IN)
        if (currentZoom != zoom) {
            currentZoom = zoom
            Gdx.graphics.requestRendering()
        }

        GameContext.zoomLevelAlpha = zoomLevelAlphaFor(zoom)
    }

    inputEvents.clear()
}

private fun zoomLevelAlphaFor(zoom: Float): Float =
    when {
        zoom < MinefieldStage.ZOOM_ALPHA_FADE_START -> 1.0f
        zoom > MinefieldStage.ZOOM_ALPHA_FADE_END -> 0.0f
        else -> (MinefieldStage.ZOOM_ALPHA_FADE_START - zoom)
    }
