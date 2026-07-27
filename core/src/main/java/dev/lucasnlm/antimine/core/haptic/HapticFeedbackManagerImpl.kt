package dev.lucasnlm.antimine.core.haptic

import android.app.Application
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dev.lucasnlm.antimine.preferences.PreferencesRepositoryImpl

class HapticFeedbackManagerImpl(
    application: Application,
    private val preferencesRepository: PreferencesRepositoryImpl,
) : HapticFeedbackManager {

    private val vibrator by lazy {
        val context = application.applicationContext

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
    }

    override fun longPressFeedback() {
        vibrateTo(SHORT_TAP_TIME_MS, SHORT_TAP_AMPLITUDE)
        vibrateTo(LONG_PRESS_TIME_MS, LONG_PRESS_AMPLITUDE)
    }

    override fun explosionFeedback() {
        vibrateTo(EXPLOSION_TIME_MS, DEFAULT_AMPLITUDE)
    }

    override fun tutorialErrorFeedback() {
        vibrateTo(SHORT_TAP_TIME_MS, SHORT_TAP_AMPLITUDE)
        vibrateTo(LONG_PRESS_TIME_MS, LONG_PRESS_AMPLITUDE)
        vibrateTo(SHORT_TAP_TIME_MS, SHORT_TAP_AMPLITUDE)
    }

    private fun vibrateTo(
        time: Long,
        amplitude: Int,
    ) {
        runCatching {
            val feedbackLevel = preferencesRepository.getHapticFeedbackLevel().toDouble() / HAPTIC_LEVEL_MAX
            val realAmplitude = (feedbackLevel * amplitude).toInt()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(time, realAmplitude),
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(time)
            }
        }
    }

    private companion object {
        const val SHORT_TAP_TIME_MS = 70L
        const val SHORT_TAP_AMPLITUDE = 240
        const val LONG_PRESS_TIME_MS = 10L
        const val LONG_PRESS_AMPLITUDE = 100
        const val EXPLOSION_TIME_MS = 400L
        const val DEFAULT_AMPLITUDE = -1
        const val HAPTIC_LEVEL_MAX = 100.0
    }
}
