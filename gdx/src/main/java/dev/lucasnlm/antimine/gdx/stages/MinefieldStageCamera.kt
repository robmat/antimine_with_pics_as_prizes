package dev.lucasnlm.antimine.gdx.stages

import android.util.SizeF
import com.badlogic.gdx.Gdx
import dev.lucasnlm.antimine.preferences.models.Minefield

/**
 * [MinefieldStage]'s camera-centering and minefield-size binding logic,
 * split out of the class body - see its class doc.
 */
fun MinefieldStage.bindSize(newMinefield: Minefield?) {
    minefield = newMinefield
    minefieldSize =
        newMinefield?.let {
            SizeF(
                it.width * renderSettings.areaSize,
                it.height * renderSettings.areaSize,
            )
        }
    onChangeGame()
}

internal fun MinefieldStage.centerCamera() {
    this.minefieldSize?.let {
        val virtualWidth = Gdx.graphics.width
        val virtualHeight = Gdx.graphics.height
        val padding = renderSettings.internalPadding

        val start = MinefieldStage.CENTER_FACTOR * virtualWidth - padding.start
        val end = it.width - MinefieldStage.CENTER_FACTOR * virtualWidth + padding.end
        val top = it.height - MinefieldStage.CENTER_FACTOR * (virtualHeight - padding.top)
        val bottom = MinefieldStage.CENTER_FACTOR * virtualHeight + padding.bottom - renderSettings.navigationBarHeight

        camera.run {
            position.set(
                (start + end) * MinefieldStage.CENTER_FACTOR,
                (top + bottom) * MinefieldStage.CENTER_FACTOR,
                0f,
            )
            update(true)
        }

        Gdx.graphics.requestRendering()
    }
}

fun MinefieldStage.onChangeGame() {
    centerCamera()
}
