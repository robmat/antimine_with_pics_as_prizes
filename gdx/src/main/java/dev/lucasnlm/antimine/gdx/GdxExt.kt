package dev.lucasnlm.antimine.gdx

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Color.WHITE
import com.badlogic.gdx.graphics.Color.argb8888ToColor
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor

fun Batch.drawRegion(
    texture: TextureRegion,
    bounds: DrawBounds,
    blend: Boolean,
    color: Color? = null,
) {
    if (blend && !isBlendingEnabled) {
        enableBlending()
    }

    setColor(color ?: WHITE)
    draw(texture, bounds.x, bounds.y, bounds.width, bounds.height)
}

fun Actor.drawAsset(
    batch: Batch,
    texture: TextureRegion,
    color: Color? = null,
    blend: Boolean = true,
    scale: Float = 1.0f,
) {
    if (blend && !batch.isBlendingEnabled) {
        batch.enableBlending()
    }

    batch.run {
        setColor(color ?: WHITE)
        draw(
            texture,
            x - width * (scale - 1.0f) * CENTER_OFFSET_FACTOR,
            y - height * (scale - 1.0f) * CENTER_OFFSET_FACTOR,
            width * scale,
            height * scale,
        )
    }
}

fun Int.toGdxColor(alpha: Float? = 1.0f): Color {
    val color = Color()
    argb8888ToColor(color, this)
    color.a = alpha ?: 1.0f
    return color
}

fun Int.toInverseBackOrWhite(alpha: Float? = 1.0f): Color {
    val sumRgb = (
        android.graphics.Color.red(this) +
            android.graphics.Color.green(this) +
            android.graphics.Color.blue(this)
        )

    val value =
        if (sumRgb > (BRIGHT_CHANNEL_THRESHOLD * RGB_CHANNEL_COUNT)) {
            DARK_INVERSE_VALUE
        } else {
            1.0f
        }

    return Color(value, value, value, alpha ?: 1.0f)
}

fun Color.alpha(newAlpha: Float): Color {
    a = newAlpha
    return this
}

fun Color.dim(value: Float): Color {
    r *= value
    g *= value
    b *= value
    return this
}

private const val CENTER_OFFSET_FACTOR = 0.5f
private const val BRIGHT_CHANNEL_THRESHOLD = 160
private const val RGB_CHANNEL_COUNT = 3
private const val DARK_INVERSE_VALUE = 0.15f
