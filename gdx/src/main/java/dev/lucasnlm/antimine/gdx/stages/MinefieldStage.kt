package dev.lucasnlm.antimine.gdx.stages

import android.util.SizeF
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.gdx.BuildConfig
import dev.lucasnlm.antimine.gdx.GameContext
import dev.lucasnlm.antimine.gdx.PixelPerfectViewport
import dev.lucasnlm.antimine.gdx.actors.AreaActor
import dev.lucasnlm.antimine.gdx.controller.CameraController
import dev.lucasnlm.antimine.gdx.dim
import dev.lucasnlm.antimine.gdx.events.GdxEvent
import dev.lucasnlm.antimine.gdx.models.ActionSettings
import dev.lucasnlm.antimine.gdx.models.RenderSettings
import dev.lucasnlm.antimine.gdx.toGdxColor
import dev.lucasnlm.antimine.gdx.toInverseBackOrWhite
import dev.lucasnlm.antimine.preferences.models.Minefield

class MinefieldStage(
    val screenWidth: Float,
    val screenHeight: Float,
    private var actionSettings: ActionSettings,
    private val renderSettings: RenderSettings,
    private val onSingleTap: (Int) -> Unit,
    private val onDoubleTap: (Int) -> Unit,
    private val onLongTouch: (Int) -> Unit,
    private val onEngineReady: () -> Unit,
) : Stage(PixelPerfectViewport(screenWidth, screenHeight)) {
    private var minefield: Minefield? = null
    private var minefieldSize: SizeF? = null
    private var currentZoom: Float = 1.0f

    private var lastCameraPosition: Vector3? = null
    private var lastZoom: Float? = null

    private val cameraController: CameraController

    private var forceRefreshVisibleAreas = true
    private var boundAreas = listOf<Area>()
    private var newBoundAreas: List<Area>? = null

    private var inputInit: Long = 0L
    private val inputEvents: MutableList<GdxEvent> = mutableListOf()

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

    private fun zoomLevelAlphaFor(zoom: Float): Float =
        when {
            zoom < ZOOM_ALPHA_FADE_START -> 1.0f
            zoom > ZOOM_ALPHA_FADE_END -> 0.0f
            else -> (ZOOM_ALPHA_FADE_START - zoom)
        }

    fun setZoom(value: Float) {
        (camera as OrthographicCamera).apply {
            zoom = value.coerceIn(SET_ZOOM_MIN, MAX_ZOOM_IN)
            currentZoom = zoom
            update(true)

            GameContext.zoomLevelAlpha = zoomLevelAlphaFor(zoom)
        }

        inputEvents.clear()
    }

    fun scaleZoom(zoomMultiplier: Float) {
        (camera as OrthographicCamera).apply {
            val newZoom =
                if (zoomMultiplier > 1.0) {
                    zoom + 1.0f * Gdx.graphics.deltaTime
                } else {
                    zoom - 1.0f * Gdx.graphics.deltaTime
                }
            zoom = newZoom.coerceIn(MAX_ZOOM_OUT, MAX_ZOOM_IN)
            if (currentZoom != zoom) {
                currentZoom = zoom
                Gdx.graphics.requestRendering()
            }

            GameContext.zoomLevelAlpha = zoomLevelAlphaFor(zoom)
        }

        inputEvents.clear()
    }

    fun bindField(field: List<Area>) {
        newBoundAreas = field
        forceRefreshVisibleAreas = true
    }

    private fun rebuildActors(boundAreas: List<Area>) {
        clear()
        if (boundAreas.size < actors.size) {
            root.children.shrink()
        } else {
            root.children.ensureCapacity(boundAreas.size)
        }

        boundAreas.forEach {
            addActor(
                AreaActor(
                    theme = renderSettings.theme,
                    size = renderSettings.areaSize,
                    area = it,
                    field = boundAreas,
                    onInputEvent = ::handleGameEvent,
                    enableLigatures = renderSettings.joinAreas,
                ),
            )
        }
        refreshVisibleActorsIfNeeded()
    }

    private fun updateExistingActors(boundAreas: List<Area>) {
        val reset = boundAreas.count { it.hasMine } == 0

        actors.forEach {
            if (it.isVisible) {
                val areaActor = (it as AreaActor)
                val area = boundAreas[areaActor.boundAreaId()]
                areaActor.bindArea(reset, renderSettings.joinAreas, area, boundAreas)
            }
        }
    }

    private fun refreshAreas(forceRefresh: Boolean) {
        if (forceRefresh || forceRefreshVisibleAreas) {
            val boundAreas = newBoundAreas ?: this.boundAreas

            newBoundAreas?.let {
                this.boundAreas = it
                this.newBoundAreas = null
            }

            if (actors.size != boundAreas.size) {
                rebuildActors(boundAreas)
            } else {
                updateExistingActors(boundAreas)
            }

            onEngineReady()

            forceRefreshVisibleAreas = false
            Gdx.graphics.requestRendering()
        }
    }

    fun bindSize(newMinefield: Minefield?) {
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

    private fun centerCamera() {
        this.minefieldSize?.let {
            val virtualWidth = Gdx.graphics.width
            val virtualHeight = Gdx.graphics.height
            val padding = renderSettings.internalPadding

            val start = CENTER_FACTOR * virtualWidth - padding.start
            val end = it.width - CENTER_FACTOR * virtualWidth + padding.end
            val top = it.height - CENTER_FACTOR * (virtualHeight - padding.top)
            val bottom = CENTER_FACTOR * virtualHeight + padding.bottom - renderSettings.navigationBarHeight

            camera.run {
                position.set((start + end) * CENTER_FACTOR, (top + bottom) * CENTER_FACTOR, 0f)
                update(true)
            }

            Gdx.graphics.requestRendering()
        }
    }

    fun onChangeGame() {
        centerCamera()
    }

    private fun handleGameEvent(gdxEvent: GdxEvent) {
        if (inputEvents.firstOrNull { it.id != gdxEvent.id } != null) {
            inputEvents.clear()
        }

        if (inputEvents.firstOrNull { it is GdxEvent.TouchUpEvent } == null) {
            inputInit = System.currentTimeMillis()
        }

        if (gdxEvent is GdxEvent.TouchUpEvent && inputEvents.firstOrNull { it is GdxEvent.TouchDownEvent } == null) {
            // Ignore unpaired up event
            return
        }

        inputEvents.add(gdxEvent)
    }

    private fun handleTapWithDoubleTapSupport(
        dt: Long,
        touchUpEvents: List<GdxEvent.TouchUpEvent>,
    ) {
        if (dt > actionSettings.doubleTapTimeout) {
            touchUpEvents.groupBy { it.id }
                .entries
                .first()
                .let {
                    when (it.value.count()) {
                        1 -> onSingleTap(it.key)
                        2 -> onDoubleTap(it.key)
                        else -> {
                        }
                    }
                }.also {
                    inputEvents.clear()
                }
        }
    }

    private fun handleTouchUp(
        dt: Long,
        touchUpEvents: List<GdxEvent.TouchUpEvent>,
        touchDownEvents: List<GdxEvent.TouchDownEvent>,
    ) {
        if (touchUpEvents.size == touchDownEvents.size) {
            if (actionSettings.handleDoubleTaps) {
                handleTapWithDoubleTapSupport(dt, touchUpEvents)
            } else {
                touchUpEvents.map { it.id }
                    .first()
                    .run(onSingleTap)
                    .also {
                        inputEvents.clear()
                    }
            }
        }

        Gdx.graphics.requestRendering()
    }

    private fun handleTouchDown(
        dt: Long,
        touchDownEvents: List<GdxEvent.TouchDownEvent>,
    ) {
        if (dt > actionSettings.longTapTimeout) {
            touchDownEvents.map { it.id }
                .first()
                .run(onLongTouch)
                .also {
                    inputEvents.clear()
                }
        }

        Gdx.graphics.requestRendering()
    }

    private fun checkGameTouchInput(now: Long) {
        if (inputEvents.isEmpty()) {
            return
        }

        val dt = now - inputInit
        val touchUpEvents = inputEvents.filterIsInstance<GdxEvent.TouchUpEvent>()
        val touchDownEvents = inputEvents.filterIsInstance<GdxEvent.TouchDownEvent>()

        if (touchUpEvents.isNotEmpty()) {
            handleTouchUp(dt, touchUpEvents, touchDownEvents)
        } else if (touchDownEvents.isNotEmpty()) {
            handleTouchDown(dt, touchDownEvents)
        }
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

    private fun refreshVisibleActorsIfNeeded(): Boolean {
        val camera = camera as OrthographicCamera
        val cameraChanged: Boolean = !camera.position.epsilonEquals(lastCameraPosition) || lastZoom != camera.zoom
        if (cameraChanged || forceRefreshVisibleAreas) {
            lastCameraPosition = camera.position.cpy()
            lastZoom = camera.zoom
        }
        return cameraChanged
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
