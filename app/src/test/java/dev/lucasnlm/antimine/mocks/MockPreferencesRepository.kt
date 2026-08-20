package dev.lucasnlm.antimine.mocks

import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.preferences.models.Action
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.preferences.models.Minefield

class MockPreferencesRepository : PreferencesRepository {
    private var customMinefield = Minefield(9, 9, 9)

    override fun hasCustomizations(): Boolean = true

    override fun reset() { /* no-op */ }

    override fun hasControlCustomizations(): Boolean = false

    override fun resetControls() { /* no-op */ }

    override fun customGameMode(): Minefield = customMinefield

    override fun updateCustomGameMode(minefield: Minefield) {
        customMinefield = minefield
    }

    override fun forgetCustomSeed() { /* no-op */ }

    override fun controlStyle(): ControlStyle = ControlStyle.Standard

    override fun hasCustomControlStyle(): Boolean = false

    override fun useControlStyle(controlStyle: ControlStyle) { /* no-op */ }

    override fun isFirstUse(): Boolean = false

    override fun completeFirstUse() { /* no-op */ }

    override fun isTutorialCompleted(): Boolean = true

    override fun setCompleteTutorial(value: Boolean) { /* no-op */ }

    override fun customLongPressTimeout(): Long = 400L

    override fun setCustomLongPressTimeout(value: Long) { /* no-op */ }

    override fun getDoubleClickTimeout(): Long = 400L

    override fun setDoubleClickTimeout(value: Long) { /* no-op */ }

    override fun themeId(): Long = 1L

    override fun useTheme(themeId: Long) { /* no-op */ }

    override fun skinId(): Long = 1L

    override fun useSkin(skinId: Long) { /* no-op */ }

    override fun setPreferredLocale(locale: String) { /* no-op */ }

    override fun getPreferredLocale(): String = "en"

    override fun updateStatsBase(statsBase: Int) { /* no-op */ }

    override fun getStatsBase(): Int = 0

    override fun getUseCount(): Int = 10

    override fun incrementUseCount() { /* no-op */ }

    override fun incrementProgressiveValue() { /* no-op */ }

    override fun decrementProgressiveValue() { /* no-op */ }

    override fun getProgressiveValue(): Int = 0

    override fun isRequestRatingEnabled(): Boolean = false

    override fun disableRequestRating() { /* no-op */ }

    override fun setPremiumFeatures(status: Boolean) { /* no-op */ }

    override fun isPremiumEnabled(): Boolean = false

    override fun setShowSupport(show: Boolean) { /* no-op */ }

    override fun showSupport(): Boolean = true

    override fun useHelp(): Boolean = false

    override fun useSimonTathamAlgorithm(): Boolean = true

    override fun setSimonTathamAlgorithm(enabled: Boolean) { /* no-op */ }

    override fun lastHelpUsed(): Long = 0L

    override fun refreshLastHelpUsed() { /* no-op */ }

    override fun setHelp(value: Boolean) { /* no-op */ }

    override fun getTips(): Int = 0

    override fun setTips(tips: Int) { /* no-op */ }

    override fun getExtraTips(): Int = 5

    override fun setExtraTips(tips: Int) { /* no-op */ }

    override fun getSwitchControlAction(): Action = Action.OpenTile

    override fun setSwitchControl(action: Action) {
        TODO("Not yet implemented")
    }

    override fun useFlagAssistant(): Boolean = false

    override fun setFlagAssistant(value: Boolean) { /* no-op */ }

    override fun useHapticFeedback(): Boolean = true

    override fun setHapticFeedback(value: Boolean) { /* no-op */ }

    override fun useAnimations(): Boolean = false

    override fun setAnimations(enabled: Boolean) { /* no-op */ }

    override fun useQuestionMark(): Boolean = false

    override fun setQuestionMark(value: Boolean) { /* no-op */ }

    override fun isSoundEffectsEnabled(): Boolean = false

    override fun setSoundEffectsEnabled(value: Boolean) { /* no-op */ }

    override fun isMusicEnabled(): Boolean = false

    override fun setMusicEnabled(value: Boolean) { /* no-op */ }

    override fun touchSensibility(): Int = 35

    override fun setTouchSensibility(sensibility: Int) { /* no-op */ }

    override fun showWindowsWhenFinishGame(): Boolean = true

    override fun mustShowWindowsWhenFinishGame(enabled: Boolean) { /* no-op */ }

    override fun openGameDirectly(): Boolean = false

    override fun setOpenGameDirectly(value: Boolean) { /* no-op */ }

    override fun userId(): String? = null

    override fun setUserId(userId: String) { /* no-op */ }

    override fun showTutorialDialog(): Boolean = false

    override fun setTutorialDialog(show: Boolean) { /* no-op */ }

    override fun allowTapOnNumbers(): Boolean = true

    override fun setAllowTapOnNumbers(allow: Boolean) { /* no-op */ }

    override fun getHapticFeedbackLevel(): Int = 100

    override fun setHapticFeedbackLevel(value: Int) { /* no-op */ }

    override fun resetHapticFeedbackLevel() { /* no-op */ }

    override fun showTutorialButton(): Boolean = true

    override fun setShowTutorialButton(value: Boolean) { /* no-op */ }

    override fun showMusicBanner(): Boolean = true

    override fun setShowMusicBanner(value: Boolean) { /* no-op */ }

    override fun lastMusicBanner(): Long = 0L

    override fun setLastMusicBanner(value: Long) { /* no-op */ }

    override fun dimNumbers(): Boolean = false

    override fun setDimNumbers(value: Boolean) { /* no-op */ }

    override fun setRequestDonation(request: Boolean) { /* no-op */ }

    override fun requestDonation(): Boolean = true

    override fun letNumbersAutoFlag(): Boolean = true

    override fun setNumbersAutoFlag(allow: Boolean) { /* no-op */ }

    override fun showTimer(): Boolean = true

    override fun setTimerVisible(visible: Boolean) { /* no-op */ }

    override fun showContinueGame() = true

    override fun setContinueGameLabel(value: Boolean) { /* no-op */ }

    override fun showNewThemesIcon(): Boolean = false

    override fun setNewThemesIcon(visible: Boolean) { /* no-op */ }

    override fun exportData(): Map<String, Any?> = mapOf()

    override fun importData(data: Map<String, Any?>) { /* no-op */ }

    override fun keepRequestPlayGames(): Boolean = false

    override fun setRequestPlayGames(showRequest: Boolean) { /* no-op */ }

    override fun lastAppVersion(): Int = 0

    override fun setLastAppVersion(versionCode: Int) { /* no-op */ }
}
