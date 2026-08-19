package dev.lucasnlm.antimine.gdx

import android.content.Context
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.input.GestureDetector
import dev.lucasnlm.antimine.core.AppVersionManager
import dev.lucasnlm.antimine.core.isPortrait
import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.core.repository.DimensionRepository
import dev.lucasnlm.antimine.gdx.controller.GameInputController
import dev.lucasnlm.antimine.gdx.models.ActionSettings
import dev.lucasnlm.antimine.gdx.models.GameInputCallbacks
import dev.lucasnlm.antimine.gdx.models.RenderSettings
import dev.lucasnlm.antimine.gdx.stages.MinefieldStage
import dev.lucasnlm.antimine.gdx.stages.bindSize
import dev.lucasnlm.antimine.gdx.stages.onChangeGame
import dev.lucasnlm.antimine.gdx.stages.scaleZoom
import dev.lucasnlm.antimine.gdx.stages.setZoom
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.preferences.models.Minefield
import dev.lucasnlm.antimine.ui.ext.blue
import dev.lucasnlm.antimine.ui.ext.green
import dev.lucasnlm.antimine.ui.ext.red
import dev.lucasnlm.antimine.ui.repository.ThemeRepository

/**
 * [loadGameTextures] and [getInternalPadding] were split out into
 * GameApplicationListenerTextures.kt since this class's function count was
 * over threshold; [context] and [dimensionRepository] are `internal` rather
 * than `private` only because that extraction needs access to them. The tap
 * callbacks are bundled into [GameInputCallbacks] since the constructor's
 * individual-callback parameter count was over threshold.
 */
class GameApplicationListener(
    internal val context: Context,
    private val appVersion: AppVersionManager,
    private val preferencesRepository: PreferencesRepository,
    private val themeRepository: ThemeRepository,
    internal val dimensionRepository: DimensionRepository,
    private val callbacks: GameInputCallbacks,
) : ApplicationAdapter() {
    private var minefieldStage: MinefieldStage? = null
    private var boundAreas: List<Area> = listOf()
    private var boundMinefield: Minefield? = null

    private var batch: SpriteBatch? = null

    private val renderSettings =
        RenderSettings(
            theme = themeRepository.getTheme(),
            internalPadding = getInternalPadding(),
            areaSize = dimensionRepository.areaSize(),
            navigationBarHeight = dimensionRepository.navigationBarHeight().toFloat(),
            appBarWithStatusHeight = dimensionRepository.actionBarSizeWithStatus().toFloat(),
            appBarHeight =
            if (context.isPortrait()) {
                dimensionRepository.actionBarSize().toFloat()
            } else {
                0f
            },
            joinAreas = themeRepository.getSkin().hasPadding,
        )

    private var actionSettings =
        with(preferencesRepository) {
            val control = controlStyle()
            ActionSettings(
                handleDoubleTaps = control == ControlStyle.DoubleClick || control == ControlStyle.DoubleClickInverted,
                longTapTimeout = preferencesRepository.customLongPressTimeout(),
                doubleTapTimeout = preferencesRepository.getDoubleClickTimeout(),
                touchSensibility = preferencesRepository.touchSensibility() * preferencesRepository.touchSensibility(),
            )
        }

    private val minefieldInputController =
        GameInputController(
            onChangeZoom = {
                GameContext.zoom = it
                minefieldStage?.scaleZoom(it)
            },
        )

    override fun create() {
        super.create()

        val width = Gdx.graphics.width
        val height = Gdx.graphics.height

        minefieldStage =
            MinefieldStage(
                screenWidth = width.toFloat(),
                screenHeight = height.toFloat(),
                renderSettings = renderSettings,
                actionSettings = actionSettings,
                callbacks = callbacks,
            ).apply {
                bindField(boundAreas)
                bindSize(boundMinefield)
            }

        loadGameTextures(themeRepository.getSkin())

        Gdx.input.inputProcessor = InputMultiplexer(GestureDetector(minefieldInputController), minefieldStage)
        Gdx.graphics.isContinuousRendering = false
    }

    override fun dispose() {
        super.dispose()
        batch?.dispose()

        GameContext.run {
            zoomLevelAlpha = 1.0f
            gameTextures = null
            atlas?.dispose()
            atlas = null
        }

        Gdx.input.inputProcessor = null
        boundMinefield = null
    }

    fun onPause() {
        GameContext.run {
            zoom = 1.0f
            minefieldStage?.setZoom(1.0f)
        }
    }

    override fun render() {
        super.render()
        val minefieldStage = this.minefieldStage
        val currentTheme = themeRepository.getTheme()

        if (!appVersion.isValid()) {
            Thread.sleep(INVALID_VERSION_SLEEP_MS)
        }

        minefieldStage?.run {
            currentTheme.palette.background.run {
                Gdx.gl.glClearColor(red(), green(), blue(), 1f)
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
            }

            act()
            draw()
        }
    }

    fun bindMinefield(minefield: Minefield) {
        boundMinefield = minefield
        minefieldStage?.bindSize(minefield)
        Gdx.graphics.requestRendering()
    }

    fun bindField(field: List<Area>) {
        boundAreas = field
        minefieldStage?.bindField(field)
        Gdx.graphics.requestRendering()
    }

    fun setActionsEnabled(enabled: Boolean) {
        GameContext.actionsEnabled = enabled
    }

    fun onChangeGame() {
        minefieldStage?.onChangeGame()
    }

    fun refreshZoom() {
        minefieldStage?.setZoom(GameContext.zoom)
    }

    fun refreshSettings() {
        actionSettings =
            with(preferencesRepository) {
                val control = controlStyle()
                val isDoubleClick = control == ControlStyle.DoubleClick || control == ControlStyle.DoubleClickInverted
                ActionSettings(
                    handleDoubleTaps = isDoubleClick,
                    longTapTimeout = preferencesRepository.customLongPressTimeout(),
                    doubleTapTimeout = preferencesRepository.getDoubleClickTimeout(),
                    touchSensibility = preferencesRepository.touchSensibility(),
                )
            }

        minefieldStage?.updateActionSettings(actionSettings)
    }

    private companion object {
        const val INVALID_VERSION_SLEEP_MS = 500L
    }
}
