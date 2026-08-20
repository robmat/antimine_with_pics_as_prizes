package dev.lucasnlm.antimine.preferences

/**
 * Split out of [PreferencesRepository] - see [CorePreferences]'s doc.
 */
interface FeedbackAnimationPreferences {
    fun setDimNumbers(value: Boolean)

    fun useHapticFeedback(): Boolean

    fun setHapticFeedback(value: Boolean)

    fun getHapticFeedbackLevel(): Int

    fun setHapticFeedbackLevel(value: Int)

    fun resetHapticFeedbackLevel()

    fun useAnimations(): Boolean

    fun setAnimations(enabled: Boolean)

    fun useQuestionMark(): Boolean

    fun setQuestionMark(value: Boolean)
}

internal class FeedbackAnimationPreferencesImpl(
    private val preferencesManager: PreferencesManager,
) : FeedbackAnimationPreferences {
    override fun setDimNumbers(value: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_DIM_NUMBERS, value)
    }

    override fun useHapticFeedback(): Boolean = preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_VIBRATION, true)

    override fun setHapticFeedback(value: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_VIBRATION, value)
    }

    override fun getHapticFeedbackLevel(): Int =
        preferencesManager.getInt(PreferenceKeys.PREFERENCE_VIBRATION_LEVEL, DEFAULT_HAPTIC_FEEDBACK_LEVEL)

    override fun setHapticFeedbackLevel(value: Int) {
        val newValue = value.coerceIn(0, MAX_HAPTIC_FEEDBACK_LEVEL)
        preferencesManager.putInt(PreferenceKeys.PREFERENCE_VIBRATION_LEVEL, newValue)
    }

    override fun resetHapticFeedbackLevel() {
        preferencesManager.removeKey(PreferenceKeys.PREFERENCE_VIBRATION_LEVEL)
    }

    override fun useAnimations(): Boolean = preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_ANIMATION, true)

    override fun setAnimations(enabled: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_ANIMATION, enabled)
    }

    override fun useQuestionMark(): Boolean = preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_QUESTION_MARK, false)

    override fun setQuestionMark(value: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_QUESTION_MARK, value)
    }

    private companion object {
        const val DEFAULT_HAPTIC_FEEDBACK_LEVEL = 100
        const val MAX_HAPTIC_FEEDBACK_LEVEL = 200
    }
}
