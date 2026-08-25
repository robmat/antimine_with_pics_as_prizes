package com.batodev.antimine.main

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.batodev.antimine.GameActivity
import com.batodev.antimine.custom.CustomLevelDialogFragment
import com.batodev.antimine.databinding.ActivityMainBinding
import com.batodev.antimine.l10n.GameLocaleManager
import com.batodev.antimine.main.viewmodel.MainEvent
import com.batodev.antimine.main.viewmodel.MainViewModel
import dev.lucasnlm.antimine.common.level.repository.MinefieldRepository
import dev.lucasnlm.antimine.common.level.repository.SavesRepository
import dev.lucasnlm.antimine.control.ControlActivity
import dev.lucasnlm.antimine.core.audio.GameAudioManager
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.repository.DimensionRepository
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.preferences.models.Minefield
import dev.lucasnlm.antimine.ui.ext.ThemedActivity
import dev.lucasnlm.external.AnalyticsManager
import dev.lucasnlm.external.BillingManager
import dev.lucasnlm.external.FeatureFlagManager
import dev.lucasnlm.external.InAppUpdateManager
import dev.lucasnlm.external.InstantAppManager
import dev.lucasnlm.external.PlayGamesManager
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import dev.lucasnlm.antimine.common.R as CR
import dev.lucasnlm.antimine.i18n.R as i18n

class MainActivity : ThemedActivity() {
    internal val viewModel: MainViewModel by viewModel()
    internal val playGamesManager: PlayGamesManager by inject()
    internal val preferencesRepository: PreferencesRepository by inject()
    private val minefieldRepository: MinefieldRepository by inject()
    private val dimensionRepository: DimensionRepository by inject()
    internal val analyticsManager: AnalyticsManager by inject()
    internal val featureFlagManager: FeatureFlagManager by inject()
    internal val billingManager: BillingManager by inject()
    internal val savesRepository: SavesRepository by inject()
    internal val inAppUpdateManager: InAppUpdateManager by inject()
    private val instantAppManager: InstantAppManager by inject()
    internal val preferenceRepository: PreferencesRepository by inject()
    internal val soundManager: GameAudioManager by inject()
    private val gameLocaleManager: GameLocaleManager by inject()

    internal val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val playGamesFlow = PlayGamesFlow(this)
    private val menuButtonsBinder = MenuButtonsBinder(this)

    private lateinit var viewPager: ViewPager2
    internal lateinit var googlePlayLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Must be called after onCreate
        gameLocaleManager.applyPreferredLocaleIfNeeded()

        setContentView(binding.root)

        googlePlayLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    playGamesFlow.handleResult(result.data)
                }
            }

        menuButtonsBinder.bind()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && instantAppManager.isEnabled(applicationContext)) {
            listOf(
                Difficulty.Beginner,
                Difficulty.Intermediate,
                Difficulty.Expert,
                Difficulty.Master,
            ).forEach(::pushShortcutOf)
        }

        lifecycleScope.launch {
            viewModel
                .observeSideEffects()
                .collect(::handleSideEffects)
        }

        playGamesFlow.launch()

        onBackPressedDispatcher.addCallback {
            handleBackPressed()
        }

        redirectToGame()
    }

    /**
     * Pushes a shortcut to the Home launcher.
     * @param difficulty The difficulty to be used as a shortcut.
     */
    private fun shortcutLabelAndIconFor(difficulty: Difficulty): Pair<Int, Int>? =
        when (difficulty) {
            Difficulty.Beginner -> i18n.string.beginner to CR.mipmap.shortcut_one
            Difficulty.Intermediate -> i18n.string.intermediate to CR.mipmap.shortcut_two
            Difficulty.Expert -> i18n.string.expert to CR.mipmap.shortcut_three
            Difficulty.Master -> i18n.string.master to CR.mipmap.shortcut_four
            Difficulty.Legend -> i18n.string.legend to CR.mipmap.shortcut_four
            else -> null
        }

    internal fun pushShortcutOf(difficulty: Difficulty) {
        if (instantAppManager.isEnabled(applicationContext)) {
            // Ignore. Instant App doesn't support shortcuts.
            return
        }

        val idLow = difficulty.id.lowercase()
        val deeplink = "app://antimine/game?difficulty=$idLow".toUri()

        val (name, icon) = shortcutLabelAndIconFor(difficulty) ?: return

        val shortcut =
            ShortcutInfoCompat
                .Builder(applicationContext, difficulty.id)
                .setShortLabel(getString(name))
                .setIcon(IconCompat.createWithResource(applicationContext, icon))
                .setIntent(Intent(Intent.ACTION_VIEW, deeplink))
                .build()

        ShortcutManagerCompat.pushDynamicShortcut(applicationContext, shortcut)
    }

    override fun onResume() {
        super.onResume()

        if (!binding.newGameShow.isVisible) {
            binding.newGameShow.isVisible = true
            binding.difficulties.isVisible = false
        }
    }

    internal fun getDifficultyExtra(difficulty: Difficulty): String =
        minefieldRepository
            .fromDifficulty(
                difficulty,
                dimensionRepository,
                preferencesRepository,
            ).toExtraString()

    private fun redirectToGame() {
        val playGames = playGamesManager.hasGooglePlayGames()
        val openDirectly = preferencesRepository.openGameDirectly()
        val canOpenGameDirectly = ((playGames && preferencesRepository.userId() != null) || !playGames) && openDirectly
        if (canOpenGameDirectly) {
            Intent(this, GameActivity::class.java).run { startActivity(this) }
        }
    }

    private fun Minefield.toExtraString(): String =
        getString(i18n.string.minefield_with_mines_size, width, height, mines)

    private fun handleSideEffects(event: MainEvent) {
        when (event) {
            is MainEvent.ShowCustomDifficultyDialogEvent -> {
                showCustomLevelDialog()
            }

            is MainEvent.GoToMainPageEvent -> {
                viewPager.setCurrentItem(0, true)
            }

            is MainEvent.OpenActivity -> {
                startActivity(event.intent)
            }

            is MainEvent.GoToSettingsPageEvent -> {
                viewPager.setCurrentItem(1, true)
            }

            is MainEvent.ShowControlsEvent -> {
                startActivity(Intent(this, ControlActivity::class.java))
            }

            is MainEvent.ShowGooglePlayGamesEvent -> {
                playGamesFlow.show()
            }

            is MainEvent.Recreate -> {
                finish()
                startActivity(Intent(this, MainActivity::class.java))
                compatOverridePendingTransition()
            }

            else -> {
            }
        }
    }

    private fun showCustomLevelDialog() {
        if (supportFragmentManager.findFragmentByTag(CustomLevelDialogFragment.TAG) == null && !isFinishing) {
            CustomLevelDialogFragment().apply {
                show(supportFragmentManager, CustomLevelDialogFragment.TAG)
            }
        }
    }

    private fun handleBackPressed() {
        if (!binding.newGameShow.isVisible) {
            binding.newGameShow.isVisible = true
            binding.difficulties.isVisible = false
            soundManager.playClickSound(1)
        } else {
            finishAffinity()
        }
    }

    companion object {
        val TAG = MainActivity::class.simpleName
    }
}
