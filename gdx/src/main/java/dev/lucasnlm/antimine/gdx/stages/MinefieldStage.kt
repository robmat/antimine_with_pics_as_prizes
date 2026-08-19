package dev.lucasnlm.antimine.gdx.stages

import android.util.SizeF
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.gdx.BuildConfig
import dev.lucasnlm.antimine.gdx.GameContext
import dev.lucasnlm.antimine.gdx.PixelPerfectViewport
import dev.lucasnlm.antimine.gdx.controller.CameraController
import dev.lucasnlm.antimine.gdx.dim
import dev.lucasnlm.antimine.gdx.events.GdxEvent
import dev.lucasnlm.antimine.gdx.models.ActionSettings
import dev.lucasnlm.antimine.gdx.models.GameInputCallbacks
import dev.lucasnlm.antimine.gdx.models.RenderSettings
import dev.lucasnlm.antimine.gdx.toGdxColor
import dev.lucasnlm.antimine.gdx.toInverseBackOrWhite
import dev.lucasnlm.antimine.preferences.models.Minefield

/**
 * This class's function count and constructor parameter count were over
 * detekt's thresholds. The tap callbacks are bundled into
 * [GameInputCallbacks]; zoom control moved to MinefieldStageZoom.kt, actor
 * rebuild/refresh logic to MinefieldStageActors.kt, camera-centering/size
 * binding to MinefieldStageCamera.kt, and touch-event bookkeeping to
 * MinefieldStageInput.kt. Most fields are `internal` rather than `private`
 * only because those extension functions, living outside the class body,
 * need access to them.
 */
class MinefieldStage(
    val screenWidth: Float,
    val screenHeight: Float,
    internal var actionSettings: ActionSettings,
    internal val renderSettings: RenderSettings,
    internal val callbacks: GameInputCallbacks,
) : Stage(PixelPerfectViewport(screenWidth, screenHeight)) {
    internal var minefield: Minefield? = null
    internal var minefieldSize: SizeF? = null
    internal var currentZoom: Float = 1.0f

    internal var lastCameraPosition: Vector3? = null
    internal var lastZoom: Float? = null

    internal val cameraController: CameraController

    internal var forceRefreshVisibleAreas = true
    internal var boundAreas = listOf<Area>()
    internal var newBoundAreas: List<Area>? = null

    internal var inputInit: Long = 0L
    internal val inputEvents: MutableList<GdxEvent> = mutableListOf()

    init {
        actionsRequestRendering = true

        addListener(
            object : InputListener() {
                override fun touchDown(
                    event: InputEvent?,
                    x: Float,
                    y: Float,
                    pointer: Int,
                    button: Int,
                ): Boolean {
                    return if (event?.target is Group) {
                        event.cancel()
                        true
                    } else {
                        false
                    }
                }
            },
        )

        cameraController =
            CameraController(
                camera = camera,
                renderSettings = renderSettings,
            )
    }

    fun bindField(field: List<Area>) {
        newBoundAreas = field
        forceRefreshVisibleAreas = true
    }

    override fun act() {
        super.act()

        GameContext.apply {
            val theme = renderSettings.theme
            backgroundColor =
                if (theme.isDarkTheme && canTintAreas) {
                    theme.palette.covered.toGdxColor(DARK_THEME_BG_ALPHA_FACTOR * zoomLevelAlpha)
                } else {
                    theme.palette.background.toInverseBackOrWhite(LIGHT_THEME_BG_ALPHA_FACTOR * zoomLevelAlpha)
                }
            coveredAreaColor = theme.palette.covered.toGdxColor(1.0f)
            coveredMarkedAreaColor = theme.palette.covered.toGdxColor(1.0f).dim(MARKED_AREA_DIM)
            markColor =
                if (canTintAreas) {
                    theme.palette.covered.toInverseBackOrWhite(TINTED_MARK_ALPHA)
                } else {
                    whiteColor
                }
        }

        checkGameTouchInput(System.currentTimeMillis())

        // Handle camera movement
        minefieldSize?.let { cameraController.act(it) }

        val forceRefresh = refreshVisibleActorsIfNeeded()
        refreshAreas(forceRefresh)

        if (BuildConfig.DEBUG) {
            Gdx.app.log("GDX", "GDX FPS = ${Gdx.graphics.framesPerSecond}")
        }
    }

    override fun touchDown(
        screenX: Int,
        screenY: Int,
        pointer: Int,
        button: Int,
    ): Boolean {
        Gdx.graphics.isContinuousRendering = true
        return super.touchDown(screenX, screenY, pointer, button)
    }

    override fun touchUp(
        screenX: Int,
        screenY: Int,
        pointer: Int,
        button: Int,
    ): Boolean {
        cameraController.freeTouch()
        Gdx.graphics.isContinuousRendering = false
        return super.touchUp(screenX, screenY, pointer, button)
    }

    override fun touchDragged(
        screenX: Int,
        screenY: Int,
        pointer: Int,
    ): Boolean {
        return minefieldSize?.let {
            val dx = Gdx.input.deltaX.toFloat()
            val dy = Gdx.input.deltaY.toFloat()

            if (dx * dx + dy * dy > actionSettings.touchSensibility * DRAG_THRESHOLD_MULTIPLIER) {
                inputEvents.clear()
            }

            cameraController.startTouch(
                x = screenX.toFloat(),
                y = screenY.toFloat(),
            )

            cameraController.translate(
                dx = -dx * currentZoom,
                dy = dy * currentZoom,
                x = screenX.toFloat(),
                y = screenY.toFloat(),
            )

            true
        } != null
    }

    fun updateActionSettings(actionSettings: ActionSettings) {
        this.actionSettings = actionSettings
    }

    companion object {
        const val MAX_ZOOM_OUT = 0.35f
        const val MAX_ZOOM_IN = 3.0f
        const val SET_ZOOM_MIN = 0.8f
        const val ZOOM_ALPHA_FADE_START = 3.5f
        const val ZOOM_ALPHA_FADE_END = 4.0f
        const val CENTER_FACTOR = 0.5f
        const val DARK_THEME_BG_ALPHA_FACTOR = 0.035f
        const val LIGHT_THEME_BG_ALPHA_FACTOR = 0.1f
        const val MARKED_AREA_DIM = 0.6f
        const val TINTED_MARK_ALPHA = 0.8f
        const val DRAG_THRESHOLD_MULTIPLIER = 8
    }
}
