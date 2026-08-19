package com.batodev.antimine

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.addCallback
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.core.os.ConfigurationCompat
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.badlogic.gdx.backends.android.AndroidFragmentApplication
import com.batodev.antimine.databinding.ActivityGameBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dev.lucasnlm.antimine.common.level.repository.SavesRepository
import dev.lucasnlm.antimine.common.level.view.GameRenderFragment
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.common.level.viewmodel.loadGame
import dev.lucasnlm.antimine.common.level.viewmodel.loadLastGame
import dev.lucasnlm.antimine.common.level.viewmodel.pauseGame
import dev.lucasnlm.antimine.common.level.viewmodel.resumeGame
import dev.lucasnlm.antimine.common.level.viewmodel.retryGame
import dev.lucasnlm.antimine.common.level.viewmodel.saveGame
import dev.lucasnlm.antimine.common.level.viewmodel.startGameFromDifficultyQueryParam
import dev.lucasnlm.antimine.common.level.viewmodel.startNewGame
import dev.lucasnlm.antimine.core.audio.GameAudioManager
import dev.lucasnlm.antimine.core.cloud.CloudSaveManager
import dev.lucasnlm.antimine.core.isPortrait
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.serializableNonSafe
import dev.lucasnlm.antimine.gdx.GameContext
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.tutorial.TutorialActivity
import dev.lucasnlm.antimine.ui.ext.ThemedActivity
import dev.lucasnlm.antimine.ui.ext.toAndroidColor
import dev.lucasnlm.external.AdsManager
import dev.lucasnlm.external.AnalyticsManager
import dev.lucasnlm.external.FeatureFlagManager
import dev.lucasnlm.external.InstantAppManager
import dev.lucasnlm.external.PlayGamesManager
import dev.lucasnlm.external.ReviewWrapperImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.dionsegijn.konfetti.core.Position
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale
import dev.lucasnlm.antimine.i18n.R as i18n
import dev.lucasnlm.antimine.ui.R as ui

/** Formats an Int for on-screen counters (mine count, hint count) using the device locale. */
internal fun Int.toL10nString(): String = String.format(Locale.getDefault(), "%d", this)

class GameActivity :
    ThemedActivity(),
    AndroidFragmentApplication.Callbacks {

    internal val gameViewModel by viewModel<GameViewModel>()
    private val appScope: CoroutineScope by inject()
    internal val preferencesRepository: PreferencesRepository by inject()
    internal val analyticsManager: AnalyticsManager by inject()
    private val instantAppManager: InstantAppManager by inject()
    private val savesRepository: SavesRepository by inject()
    private val playGamesManager: PlayGamesManager by inject()
    internal val gameAudioManager: GameAudioManager by inject()
    internal val adsManager: AdsManager by inject()
    internal val reviewWrapper: ReviewWrapperImpl by inject()
    internal val featureFlagManager: FeatureFlagManager by inject()
    private val cloudSaveManager by inject<CloudSaveManager>()

    internal var warning: Snackbar? = null
    internal var revealBombFeedback: ValueAnimator? = null

    private val hasFloatingButton = preferencesRepository.controlStyle() == ControlStyle.SwitchMarkOpen
    internal val binding: ActivityGameBinding by lazy {
        ActivityGameBinding.inflate(layoutInflater)
    }

    private val stateRenderer = GameStateRenderer(this)
    private val eventDialogs = GameEventDialogs(this)
    internal val tipShortcut = TipShortcutController(this)

    // `usingTheme` (ThemedActivity) is a protected/inherited member only resolvable from inside
    // this class body - exposed here so the extracted GameStateRenderer/TipShortcutController
    // classes can read it.
    internal val currentTheme get() = usingTheme

    // Tracks pause state via the existing onPause/onResume overrides below - `isPaused` isn't
    // actually a member of any of this activity's supertypes (AndroidFragmentApplication.Callbacks
    // only declares exit()), so referencing it unqualified was a pre-existing, never-compiled bug.
    internal var isGamePaused = false
        private set

    @SuppressLint("MissingSuperCall")
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.run(::handleIntent)

        GameContext.zoom = 1.0f
        GameContext.zoomLevelAlpha = 1.0f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        if (!preferencesRepository.isPremiumEnabled()) {
            adsManager.start(this)
        }

        gameViewModel.apply {
            lifecycleScope.launch {
                observeState().collect { stateRenderer.render(it) }
            }
            lifecycleScope.launch {
                observeSideEffects().collect { eventDialogs.handle(it) }
            }
        }

        fun bindToolbar() {
            binding.back.apply {
                TooltipCompat.setTooltipText(this, getString(i18n.string.back))
                setColorFilter(binding.minesCount.currentTextColor)
                setOnClickListener {
                    onBackPressed()
                }
            }

            binding.appBar.apply {
                setBackgroundColor(usingTheme.palette.background.toAndroidColor(APP_BAR_BACKGROUND_ALPHA))
            }

            if (applicationContext.isPortrait()) {
                binding.minesCount.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(this, ui.drawable.mine),
                    null,
                    null,
                    null,
                )
            } else {
                binding.minesCount.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    ContextCompat.getDrawable(this, ui.drawable.mine),
                    null,
                    null,
                )
            }
        }
        bindToolbar()

        if (!isFinishing) {
            lifecycleScope.launch {
                loadGameFragment()
            }
        }

        binding.tapToBegin.apply {
            setTextColor(usingTheme.palette.background.toAndroidColor())
        }
        if (preferencesRepository.showTutorialButton()) {
            binding.controlsToast.apply {
                setTextColor(usingTheme.palette.background.toAndroidColor())
            }
        }

        playGamesManager.showPlayPopUp(this)

        if (playGamesManager.hasGooglePlayGames() && playGamesManager.shouldRequestLogin()) {
            playGamesManager.keepRequestingLogin(false)
            lifecycleScope.launch {
                runCatching {
                    withContext(Dispatchers.IO) {
                        val logged = playGamesManager.silentLogin()
                        if (!logged) {
                            preferencesRepository.setUserId("")
                        }
                        playGamesManager.showPlayPopUp(this@GameActivity)
                    }
                }.onFailure {
                    Log.e(TAG, "Failed silent login", it)
                }
            }
        }

        onBackPressedDispatcher.addCallback {
            finish()
        }
        val settingsHelper = SettingsHelper(this)
        Log.d(TAG, settingsHelper.preferences.toString())
    }

    private fun handleIntent(intent: Intent) {
        lifecycleScope.launch {
            val extras = intent.extras ?: Bundle()

            if (gameViewModel.startGameFromDifficultyQueryParam(this@GameActivity, intent)) {
                bindPrizeImage()
                return@launch
            }

            when {
                extras.containsKey(DIFFICULTY) -> {
                    intent.removeExtra(DIFFICULTY)
                    val difficulty = extras.serializableNonSafe<Difficulty>(DIFFICULTY)
                    gameViewModel.startNewGame(this@GameActivity, difficulty)
                }
                extras.containsKey(RETRY_GAME) -> {
                    val uid = extras.getInt(RETRY_GAME)
                    gameViewModel.retryGame(uid, this@GameActivity)
                }
                extras.containsKey(START_GAME) -> {
                    val uid = extras.getInt(START_GAME)
                    gameViewModel.loadGame(uid, this@GameActivity)
                }
                else -> {
                    gameViewModel.loadLastGame(this@GameActivity)
                }
            }
        }
    }

    internal fun bindPrizeImage() {
        if (gameViewModel.prizeImage != "") {
            binding.gameBackground?.setImageBitmap(
                BitmapFactory.decodeStream(assets.open("$PRIZE_IMAGES/${gameViewModel.prizeImage}"))
            )
        } else {
            Log.d(TAG, "gameViewModel.prizeImage: ${gameViewModel.prizeImage}")
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasFloatingButton != (preferencesRepository.controlStyle() == ControlStyle.SwitchMarkOpen)
        ) {
            // If used changed any currently rendered settings, we
            // must recreate the activity to force all sprites are updated.
            recreate()
            return
        }

        isGamePaused = false
        analyticsManager.sentEvent(Analytics.Resume)
        keepScreenOn(true)
        gameViewModel.resumeGame()

        if (gameViewModel.singleState().isActive) {
            gameAudioManager.resumeMusic()
        }
    }

    override fun onPause() {
        super.onPause()
        isGamePaused = true
        keepScreenOn(false)

        revealBombFeedback?.removeAllListeners()
        revealBombFeedback = null

        cloudSaveManager.uploadSave()

        if (isFinishing) {
            analyticsManager.sentEvent(Analytics.Quit)
        } else if (gameViewModel.singleState().isActive) {
            gameViewModel.pauseGame()
        }

        appScope.launch {
            gameViewModel.saveGame()
        }

        gameAudioManager.pauseMusic()
    }

    override fun onDestroy() {
        super.onDestroy()
        gameAudioManager.stopMusic()
    }

    private suspend fun loadGameFragment() {
        fun onOpenAppActions() {
            if (instantAppManager.isEnabled(applicationContext)) {
                // Instant App does nothing.
                savesRepository.setLimit(1)
            } else {
                preferencesRepository.incrementUseCount()

                if (preferencesRepository.getUseCount() > featureFlagManager.minUsageToReview) {
                    reviewWrapper.startInAppReview(this)
                }
            }

            lifecycleScope.launch {
                if (preferencesRepository.showTutorialDialog()) {
                    val firstLocale = ConfigurationCompat.getLocales(resources.configuration).get(0)
                    val lang = firstLocale?.language

                    val message = getString(i18n.string.do_you_know_how_to_play)
                    val baseText = "Do you know how to play minesweeper?"

                    if (lang != null && (lang == "en" || message != baseText)) {
                        MaterialAlertDialogBuilder(this@GameActivity).run {
                            setTitle(i18n.string.tutorial)
                            setMessage(i18n.string.do_you_know_how_to_play)
                            setPositiveButton(i18n.string.open_tutorial) { _, _ ->
                                analyticsManager.sentEvent(Analytics.KnowHowToPlay(false))
                                preferencesRepository.setTutorialDialog(false)
                                val intent = Intent(this@GameActivity, TutorialActivity::class.java)
                                startActivity(intent)
                            }
                            setNegativeButton(i18n.string.close) { _, _ ->
                                analyticsManager.sentEvent(Analytics.KnowHowToPlay(true))
                                preferencesRepository.setTutorialDialog(false)
                            }
                            show()
                        }
                    }
                }
            }
        }

        supportFragmentManager.apply {
            if (findFragmentByTag(GameRenderFragment.TAG) == null) {
                val fragment =
                    withContext(Dispatchers.IO) {
                        GameRenderFragment()
                    }

                withContext(Dispatchers.Main) {
                    beginTransaction().apply {
                        replace(R.id.levelContainer, fragment, GameRenderFragment.TAG)
                        setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        commitAllowingStateLoss()
                    }
                }

                handleIntent(intent)
                onOpenAppActions()
            }
        }
    }

    internal fun keepScreenOn(enabled: Boolean) {
        window.run {
            if (enabled) {
                addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    override fun exit() {
        // LibGDX exit callback
    }

    companion object {
        val TAG = GameActivity::class.simpleName

        const val DIFFICULTY = "difficulty"
        const val START_GAME = "start_game"
        const val RETRY_GAME = "retry_game"

        const val TIP_COOLDOWN_MS = 5 * 1000L
        const val MINE_COUNTER_ANIM_COUNTER_MS = 250L
        const val LOADING_INDICATOR_MS = 500L

        const val MAX_CONFETTI_COUNT = 100
        val CONFETTI_COLORS = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def)
        val CONFETTI_POSITION = Position.Relative(0.5, 0.2)

        const val TOAST_OFFSET_Y_DP = 128

        const val ENABLED_SHORTCUT_ALPHA = 1.0f
        const val DISABLED_SHORTCUT_ALPHA = 0.3f

        const val COVERED_TINT_ALPHA = 168
        const val APP_BAR_BACKGROUND_ALPHA = 200
    }
}
