package dev.lucasnlm.antimine.gdx.actors

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import dev.lucasnlm.antimine.gdx.DrawBounds
import dev.lucasnlm.antimine.gdx.GameContext
import dev.lucasnlm.antimine.gdx.dim
import dev.lucasnlm.antimine.gdx.drawAsset
import dev.lucasnlm.antimine.gdx.drawRegion
import dev.lucasnlm.antimine.gdx.toGdxColor
import dev.lucasnlm.antimine.gdx.toInverseBackOrWhite
import dev.lucasnlm.antimine.ui.model.minesAround
import dev.lucasnlm.antimine.ui.repository.Themes

/**
 * [AreaActor]'s uncovered/pressed-state drawing logic, split out of the class
 * body - see its class doc. The covered-tile counterpart lives in
 * AreaActorRendering.kt.
 */
internal fun AreaActor.drawUncoveredIcons(batch: Batch) {
    GameContext.gameTextures?.let {
        if (area.minesAround > 0) {
            drawAsset(
                batch = batch,
                texture = it.aroundMines[area.minesAround - 1],
                color =
                    if (area.dimNumber) {
                        theme.palette
                            .minesAround(area.minesAround - 1)
                            .toGdxColor(GameContext.zoomLevelAlpha * AreaActor.DIMMED_NUMBER_ALPHA_FACTOR)
                            .dim(AreaActor.DIMMED_NUMBER_DIM_FACTOR)
                    } else {
                        theme.palette
                            .minesAround(area.minesAround - 1)
                            .toGdxColor(GameContext.zoomLevelAlpha)
                    },
            )
        } else if (area.hasMine) {
            val color = theme.palette.uncovered
            drawAsset(
                batch = batch,
                texture = it.mine,
                color = color.toInverseBackOrWhite(1.0f),
                scale = AreaActor.BASE_ICON_SCALE,
            )
        }
    }
}

internal fun AreaActor.drawFocusScaledRegion(
    batch: Batch,
    texture: TextureRegion,
    color: Color,
) {
    batch.drawRegion(
        texture = texture,
        bounds =
            DrawBounds(
                x = x - width * (focusScale - 1.0f) * 0.5f,
                y = y - height * (focusScale - 1.0f) * 0.5f,
                width = width * focusScale,
                height = height * focusScale,
            ),
        color = color,
        blend = true,
    )
}

private fun AreaActor.drawPressedCovered(
    batch: Batch,
    isOdd: Boolean,
) {
    val tint = GameContext.canTintAreas
    val coverColor =
        when {
            tint -> {
                if (isOdd) {
                    theme.palette.coveredOdd
                } else {
                    theme.palette.covered
                }
            }

            else -> {
                Themes.WHITE
            }
        }.toGdxColor(AreaActor.PRESSED_COVER_ALPHA)

    GameContext.gameTextures?.detailedArea?.let {
        batch.drawRegion(
            texture = it,
            bounds = DrawBounds(x = x, y = y, width = width, height = height),
            color = coverColor,
            blend = true,
        )

        drawFocusScaledRegion(batch, it, coverColor.dim(AreaActor.FOCUS_DIM_BASE - (focusScale - 1.0f)))
    }
}

private fun AreaActor.drawPressedUncovered(batch: Batch) {
    GameContext.gameTextures?.detailedArea?.let {
        val color = theme.palette.background
        drawFocusScaledRegion(
            batch,
            it,
            color
                .toInverseBackOrWhite(AreaActor.PRESSED_UNCOVERED_ALPHA)
                .dim(AreaActor.FOCUS_DIM_BASE - (focusScale - 1.0f)),
        )
    }
}

internal fun AreaActor.drawPressed(
    batch: Batch,
    isOdd: Boolean,
) {
    if (isPressed || focusScale > 1.0f) {
        if (area.isCovered) {
            drawPressedCovered(batch, isOdd)
        } else {
            drawPressedUncovered(batch)
        }
    }
}
