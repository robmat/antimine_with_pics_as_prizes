package com.batodev.antimine.main

import android.content.Intent
import android.os.Build
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.batodev.antimine.GalleryActivity
import com.batodev.antimine.history.HistoryActivity
import com.batodev.antimine.main.viewmodel.MainEvent
import com.batodev.antimine.preferences.PreferencesActivity
import com.batodev.antimine.stats.StatsActivity
import dev.lucasnlm.antimine.about.AboutActivity
import dev.lucasnlm.antimine.common.level.database.models.SaveStatus
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.themes.ThemeActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import dev.lucasnlm.antimine.i18n.R as i18n

/**
 * Binds every click listener / initial visibility on the main menu screen -
 * split out of [MainActivity] since this single method was the bulk of its
 * function count (and, on its own, exceeded detekt's long-method/complexity
 * limits).
 */
class MenuButtonsBinder(private val activity: MainActivity) {
    fun bind() =
        with(activity) {
            bindContinueButton()
            bindNewGameFlow()
            bindTopActions()
            bindGallerySection()
            bindSecondaryNav()
            bindStatsAboutPlayGames()
        }

    private fun MainActivity.bindContinueButton() {
        binding.continueGame.apply {
            if (preferencesRepository.showContinueGame()) {
                setText(i18n.string.continue_game)
            } else {
                setText(i18n.string.start)
            }

            setOnClickListener {
                soundManager.playClickSound()
                viewModel.sendEvent(MainEvent.ContinueGameEvent)
            }
        }

        if (!preferencesRepository.showContinueGame()) {
            lifecycleScope.launch {
                savesRepository.fetchCurrentSave()?.let {
                    preferencesRepository.setContinueGameLabel(true)
                    binding.continueGame.setText(i18n.string.continue_game)
                }
            }
        }

        lifecycleScope.launch {
            if (preferencesRepository.showTutorialButton()) {
                val shouldShowTutorial = savesRepository.getAllSaves().count { it.status == SaveStatus.VICTORY } < 2
                preferencesRepository.setShowTutorialButton(shouldShowTutorial)
                withContext(Dispatchers.Main) {
                    binding.tutorial.isVisible = shouldShowTutorial
                }
            } else {
                binding.tutorial.isVisible = false
            }
        }
    }

    private fun MainActivity.bindNewGameFlow() {
        binding.newGameShow.setOnClickListener {
            soundManager.playClickSound()
            binding.newGameShow.isVisible = false
            binding.difficulties.isVisible = true
        }

        mapOf(
            binding.standardSize to Difficulty.Standard,
            binding.fixedSizeSize to Difficulty.FixedSize,
            binding.beginnerSize to Difficulty.Beginner,
            binding.intermediateSize to Difficulty.Intermediate,
            binding.expertSize to Difficulty.Expert,
            binding.masterSize to Difficulty.Master,
            binding.legendSize to Difficulty.Legend,
        ).onEach {
            it.key.text = getDifficultyExtra(it.value)
        }

        mapOf(
            binding.startStandard to Difficulty.Standard,
            binding.startFixedSize to Difficulty.FixedSize,
            binding.startBeginner to Difficulty.Beginner,
            binding.startIntermediate to Difficulty.Intermediate,
            binding.startExpert to Difficulty.Expert,
            binding.startMaster to Difficulty.Master,
            binding.startLegend to Difficulty.Legend,
        ).forEach { (view, difficulty) ->
            view.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    pushShortcutOf(difficulty)
                }

                soundManager.playClickSound()

                viewModel.sendEvent(
                    MainEvent.StartNewGameEvent(difficulty = difficulty),
                )
            }
        }
    }

    private fun MainActivity.bindTopActions() {
        binding.startCustom.setOnClickListener {
            soundManager.playClickSound()
            analyticsManager.sentEvent(Analytics.OpenCustom)
            viewModel.sendEvent(MainEvent.ShowCustomDifficultyDialogEvent)
        }

        binding.settings.setOnClickListener {
            soundManager.playClickSound()
            analyticsManager.sentEvent(Analytics.OpenSettings)
            val intent = Intent(this, PreferencesActivity::class.java)
            startActivity(intent)
        }

        binding.moreGames.setOnClickListener {
            val i = Intent(
                Intent.ACTION_VIEW,
                "https://play.google.com/store/apps/dev?id=8228670503574649511".toUri()
            )
            startActivity(i)
        }

        binding.themes.setOnClickListener {
            soundManager.playClickSound()
            val intent = Intent(this, ThemeActivity::class.java)
            preferencesRepository.setNewThemesIcon(false)
            startActivity(intent)
        }

        binding.newThemesIcon.isVisible = preferencesRepository.showNewThemesIcon()

        binding.controls.setOnClickListener {
            soundManager.playClickSound()
            analyticsManager.sentEvent(Analytics.OpenControls)
            viewModel.sendEvent(MainEvent.ShowControlsEvent)
        }
    }

    private fun MainActivity.bindGallerySection() {
        if (featureFlagManager.isFoss) {
            binding.picturesGallleryRoot.isVisible = true
            binding.picturesGalllery.apply {
                setOnClickListener {
                    soundManager.playClickSound()
                    startActivity(Intent(context, GalleryActivity::class.java))
                }
                text = getString(i18n.string.donation)
                setIconResource(com.batodev.antimine.R.drawable.ic_round_gallery_24)
            }
        } else {
            if (!preferencesRepository.isPremiumEnabled()) {
                billingManager.start()

                lifecycleScope.launch {
                    bindPicturesGallery()

                    billingManager.getPriceFlow().collect {
                        bindPicturesGallery()
                    }
                }
            }
        }
    }

    private fun MainActivity.bindPicturesGallery() {
        binding.picturesGallleryRoot.isVisible = true
        binding.picturesGalllery.apply {
            setOnClickListener {
                soundManager.playClickSound()
                startActivity(Intent(context, GalleryActivity::class.java))
            }
            setText(i18n.string.pictures_gallery)
            setIconResource(com.batodev.antimine.R.drawable.ic_round_gallery_24)
        }
    }

    private fun MainActivity.bindSecondaryNav() {
        if (featureFlagManager.isGameHistoryEnabled) {
            binding.previousGames.setOnClickListener {
                soundManager.playClickSound()
                analyticsManager.sentEvent(Analytics.OpenSaveHistory)
                val intent = Intent(this, HistoryActivity::class.java)
                startActivity(intent)
            }
        } else {
            binding.previousGames.isVisible = false
        }

        binding.tutorial.apply {
            setText(i18n.string.tutorial)
            setOnClickListener {
                soundManager.playClickSound()
                analyticsManager.sentEvent(Analytics.OpenTutorial)
                viewModel.sendEvent(MainEvent.StartTutorialEvent)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.language.apply {
                setText(i18n.string.language)
                setOnClickListener {
                    soundManager.playClickSound()
                    analyticsManager.sentEvent(Analytics.OpenLanguage)
                    viewModel.sendEvent(MainEvent.StartLanguageEvent)
                }
            }
        } else {
            binding.language.isVisible = false
        }
    }

    private fun MainActivity.bindStatsAboutPlayGames() {
        binding.stats.setOnClickListener {
            soundManager.playClickSound()
            analyticsManager.sentEvent(Analytics.OpenStats)
            val intent = Intent(this, StatsActivity::class.java)
            startActivity(intent)
        }

        binding.about.setOnClickListener {
            soundManager.playClickSound()
            analyticsManager.sentEvent(Analytics.OpenAbout)
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }

        if (playGamesManager.hasGooglePlayGames()) {
            binding.playGames.setOnClickListener {
                soundManager.playClickSound()
                analyticsManager.sentEvent(Analytics.OpenGooglePlayGames)
                viewModel.sendEvent(MainEvent.ShowGooglePlayGamesEvent)
            }
        } else {
            binding.playGames.isVisible = false
        }
    }
}
