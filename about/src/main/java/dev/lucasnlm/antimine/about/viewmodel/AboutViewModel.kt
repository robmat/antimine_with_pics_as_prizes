package dev.lucasnlm.antimine.about.viewmodel

import android.app.Application
import android.content.Intent
import android.widget.Toast
import dev.lucasnlm.antimine.core.audio.GameAudioManager
import dev.lucasnlm.antimine.core.openExternalLink
import dev.lucasnlm.antimine.core.viewmodel.StatelessViewModel
import dev.lucasnlm.antimine.licenses.LicenseActivity
import dev.lucasnlm.antimine.i18n.R as i18n

class AboutViewModel(
    private val application: Application,
    private val audioManager: GameAudioManager,
) : StatelessViewModel<AboutEvent>() {

    override fun onEvent(event: AboutEvent) {
        when (event) {
            AboutEvent.ThirdPartyLicenses -> {
                playClickSound()
                openLicensesActivity()
            }
            AboutEvent.SourceCode -> {
                playClickSound()
                openSourceCode()
            }
            AboutEvent.Translators -> {
                playClickSound()
                openCrowdin()
            }
        }
    }

    private fun playClickSound() {
        audioManager.playClickSound()
    }

    private fun openLicensesActivity() {
        val context = application.applicationContext
        val intent =
            Intent(context, LicenseActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        context.startActivity(intent)
    }

    private fun openSourceCode() {
        val context = application.applicationContext
        context.openExternalLink(SOURCE_CODE) {
            Toast.makeText(context.applicationContext, i18n.string.unknown_error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun openCrowdin() {
        val context = application.applicationContext
        context.openExternalLink(CROWDIN_URL) {
            Toast.makeText(context.applicationContext, i18n.string.unknown_error, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val SOURCE_CODE = "https://github.com/robmat/antimine_with_pics_as_prizes"
        private const val CROWDIN_URL = "https://crowdin.com/project/antimine-android"
    }
}
