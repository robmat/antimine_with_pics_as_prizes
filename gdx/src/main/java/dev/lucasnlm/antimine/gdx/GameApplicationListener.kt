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
import dev.lucasnlm.antimine.gdx.models.GameTextures
import dev.lucasnlm.antimine.gdx.models.InternalPadding
import dev.lucasnlm.antimine.gdx.models.RenderSettings
import dev.lucasnlm.antimine.gdx.stages.MinefieldStage
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.preferences.models.Minefield
import dev.lucasnlm.antimine.ui.ext.blue
import dev.lucasnlm.antimine.ui.ext.green
import dev.lucasnlm.antimine.ui.ext.red
import dev.lucasnlm.antimine.ui.repository.ThemeRepository

class GameApplicationListener(
    private val context: Context,
    private val appVersion: AppVersionManager,
    private val preferencesRepository: PreferencesRepository,
    private val themeRepository: ThemeRepository,
    private val dimensionRepository: DimensionRepository,
    private val onSingleTap: (Int) -> Unit,
    private val onDoubleTap: (Int) -> Unit,
    private val onLongTap: (Int) -> Unit,
    private val onEngineReady: () -> Unit,
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
                onSingleTap = onSingleTap,
                onDoubleTap = onDoubleTap,
                onLongTouch = onLongTap,
                onEngineReady = onEngineReady,
            ).apply {
                bindField(boundAreas)
                bindSize(boundMinefield)
            }

        val currentSkin = themeRepository.getSkin()

        GameContext.run {
            canTintAreas = currentSkin.canTint

            atlas =
                GameTextureAtlas.loadTextureAtlas(
                    skinFile = currentSkin.file,
                    defaultBackground = currentSkin.background,
                ).apply {
                    gameTextures =
                        GameTextures(
                            areaBackground = findRegion(AtlasNames.SINGLE_BACKGROUND),
                            aroundMines =
                                listOf(
                                    AtlasNames.NUMBER_1,
                                    AtlasNames.NUMBER_2,
                                    AtlasNames.NUMBER_3,
                                    AtlasNames.NUMBER_4,
                                    AtlasNames.NUMBER_5,
                                    AtlasNames.NUMBER_6,
                                    AtlasNames.NUMBER_7,
                                    AtlasNames.NUMBER_8,
                                ).map(::findRegion),
                            pieces =
                                listOf(
                                    AtlasNames.CORE,
                                    AtlasNames.BOTTOM,
                                    AtlasNames.TOP,
                                    AtlasNames.RIGHT,
                                    AtlasNames.LEFT,
                                    AtlasNames.CORNER_TOP_LEFT,
                                    AtlasNames.CORNER_TOP_RIGHT,
                                    AtlasNames.CORNER_BOTTOM_RIGHT,
                                    AtlasNames.CORNER_BOTTOM_LEFT,
                                    AtlasNames.BORDER_CORNER_RIGHT,
                                    AtlasNames.BORDER_CORNER_LEFT,
                                    AtlasNames.BORDER_CORNER_BOTTOM_RIGHT,
                                    AtlasNames.BORDER_CORNER_BOTTOM_LEFT,
                                    AtlasNames.FILL_TOP_LEFT,
                                    AtlasNames.FILL_TOP_RIGHT,
                                    AtlasNames.FILL_BOTTOM_RIGHT,
                                    AtlasNames.FILL_BOTTOM_LEFT,
                                    AtlasNames.FULL,
                                ).associateWith(::findRegion),
                            mine = findRegion(AtlasNames.MINE),
                            flag = findRegion(AtlasNames.FLAG),
                            question = findRegion(AtlasNames.QUESTION),
                            detailedArea = findRegion(AtlasNames.SINGLE),
                        )
                }
        }

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
            Thread.sleep(500L)
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

    private fun getInternalPadding(): InternalPadding {
        val padding = dimensionRepository.areaSize()
        return when {
            context.isPortrait() -> {
                InternalPadding(
                    start = padding,
                    end = padding,
                    bottom = padding,
                    top = padding,
                )
            }
            else -> {
                InternalPadding(
                    start = padding,
                    end = padding,
                    bottom = padding,
                    top = padding,
                )
            }
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
}
