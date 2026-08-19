package dev.lucasnlm.antimine.gdx.actors

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Touchable
import dev.lucasnlm.antimine.core.getNeighborIdAtPos
import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.gdx.GameContext
import dev.lucasnlm.antimine.gdx.events.GdxEvent
import dev.lucasnlm.antimine.ui.model.AppTheme
import dev.lucasnlm.antimine.ui.model.minesAround

/**
 * [focusScale], [isPressed] and [pieces] used to be constructor parameters
 * defaulted at every call site (nobody ever overrode them), which pushed the
 * constructor's parameter count over threshold for no real benefit - they're
 * internal actor state, not caller-supplied configuration, so they're plain
 * fields instead. The actual drawing logic (drawBackground/drawCovered/etc.)
 * was split out into extension functions in AreaActorRendering.kt, since this
 * class's function count was over threshold; several fields are `internal`
 * rather than `private` only because those extension functions, living
 * outside the class body, need access to them.
 */
class AreaActor(
    size: Float,
    field: List<Area>,
    enableLigatures: Boolean,
    internal var area: Area,
    internal val theme: AppTheme,
    private val onInputEvent: (GdxEvent) -> Unit,
) : Actor() {
    internal var focusScale: Float = 1.0f
    internal var isPressed: Boolean = false
    internal val pieces: MutableMap<String, Boolean> = mutableMapOf()

    internal var areaForm: Int? = null

    private val topId: Int
    private val bottomId: Int
    private val leftId: Int
    private val rightId: Int
    private val topLeftId: Int
    private val topRightId: Int
    private val bottomLeftId: Int
    private val bottomRightId: Int

    init {
        width = size
        height = size
        x = area.posX * width
        y = area.posY * height

        addListener(
            object : InputListener() {
                override fun touchUp(
                    event: InputEvent?,
                    x: Float,
                    y: Float,
                    pointer: Int,
                    button: Int,
                ) {
                    super.touchUp(event, x, y, pointer, button)
                    onInputEvent(GdxEvent.TouchUpEvent(area.id))
                    isPressed = false
                    toBack()
                    Gdx.graphics.requestRendering()
                }

                override fun touchDown(
                    event: InputEvent,
                    x: Float,
                    y: Float,
                    pointer: Int,
                    button: Int,
                ): Boolean {
                    toFront()
                    isPressed = true
                    onInputEvent(GdxEvent.TouchDownEvent(area.id))
                    Gdx.graphics.requestRendering()
                    return true
                }
            },
        )

        topId = area.getNeighborIdAtPos(field, 0, 1)
        bottomId = area.getNeighborIdAtPos(field, 0, -1)
        leftId = area.getNeighborIdAtPos(field, -1, 0)
        rightId = area.getNeighborIdAtPos(field, 1, 0)
        topLeftId = area.getNeighborIdAtPos(field, -1, 1)
        topRightId = area.getNeighborIdAtPos(field, 1, 1)
        bottomLeftId = area.getNeighborIdAtPos(field, -1, -1)
        bottomRightId = area.getNeighborIdAtPos(field, 1, -1)

        bindArea(reset = true, ligatureEnabled = enableLigatures, area = area, field = field)
    }

    fun boundAreaId() = area.id

    fun bindArea(
        reset: Boolean,
        ligatureEnabled: Boolean,
        area: Area,
        field: List<Area>,
    ) {
        if (reset) {
            this.isPressed = false
        }

        val newForm =
            when {
                area.isCovered && ligatureEnabled -> {
                    areaFormOf(
                        NeighborLinks(
                            top = field.getOrNull(topId)?.canLinkTo(area) == true,
                            bottom = field.getOrNull(bottomId)?.canLinkTo(area) == true,
                            left = field.getOrNull(leftId)?.canLinkTo(area) == true,
                            right = field.getOrNull(rightId)?.canLinkTo(area) == true,
                            topLeft = field.getOrNull(topLeftId)?.canLinkTo(area) == true,
                            topRight = field.getOrNull(topRightId)?.canLinkTo(area) == true,
                            bottomLeft = field.getOrNull(bottomLeftId)?.canLinkTo(area) == true,
                            bottomRight = field.getOrNull(bottomRightId)?.canLinkTo(area) == true,
                        ),
                    )
                }
                else -> {
                    AREA_NO_FORM
                }
            }

        if ((area.isCovered || area.hasMine) && this.areaForm != newForm) {
            this.areaForm = newForm
            pieces.putAll(newForm.toAtlasNames())
        }

        this.area = area
    }

    override fun act(delta: Float) {
        super.act(delta)
        val area = this.area

        touchable =
            if ((area.isCovered || area.minesAround > 0) && GameContext.actionsEnabled) {
                Touchable.enabled
            } else {
                Touchable.disabled
            }

        val newFocusScale =
            if (isPressed) {
                (focusScale + Gdx.graphics.deltaTime).coerceAtMost(MAX_SCALE)
            } else {
                (focusScale - Gdx.graphics.deltaTime).coerceAtLeast(MIN_SCALE)
            }

        if (newFocusScale != focusScale) {
            focusScale = newFocusScale
            Gdx.graphics.requestRendering()
        }
    }

    override fun draw(
        batch: Batch?,
        parentAlpha: Float,
    ) {
        super.draw(batch, parentAlpha)

        batch?.run {
            val isOdd: Boolean =
                if (area.posY % 2 == 0) {
                    area.posX % 2 != 0
                } else {
                    area.posX % 2 == 0
                }

            drawBackground(this, isOdd)

            if (area.isCovered) {
                drawCovered(this)
                drawPressed(this, isOdd)
                drawCoveredIcons(this)
            } else {
                if (area.hasMine) {
                    drawMineBackground(this)
                }
                drawPressed(this, isOdd)
                drawUncoveredIcons(this)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AreaActor

        if (area != other.area) return false
        if (pieces != other.pieces) return false
        if (areaForm != other.areaForm) return false

        return true
    }

    override fun hashCode(): Int {
        var result = area.hashCode()
        result = 31 * result + pieces.hashCode()
        result = 31 * result + (areaForm?.hashCode() ?: 0)
        return result
    }

    companion object {
        const val MIN_SCALE = 1.0f
        const val MAX_SCALE = 1.15f
        const val BASE_ICON_SCALE = 0.8f
        const val MISTAKE_TINT_RED = 0.8f
        const val MISTAKE_TINT_GREEN = 0.3f
        const val MISTAKE_TINT_BLUE = 0.3f
        const val REVEALED_MINE_ALPHA = 0.65f
        const val DIMMED_NUMBER_ALPHA_FACTOR = 0.45f
        const val DIMMED_NUMBER_DIM_FACTOR = 0.5f
        const val PRESSED_COVER_ALPHA = 0.5f
        const val FOCUS_DIM_BASE = 0.8f
        const val PRESSED_UNCOVERED_ALPHA = 0.25f

        private fun Area.canLinkTo(area: Area): Boolean {
            return isCovered && mark.ligatureMask == area.mark.ligatureMask
        }
    }
}
