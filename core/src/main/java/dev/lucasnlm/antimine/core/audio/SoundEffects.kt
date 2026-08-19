package dev.lucasnlm.antimine.core.audio

/** The one-shot sound-effect half of [GameAudioManager]'s contract. */
interface SoundEffects {
    fun playWin()

    fun playBombExplosion()

    fun playClickSound(index: Int = 0)

    fun playOpenArea()

    fun playPutFlag()

    fun playOpenMultipleArea()

    fun playRevealBomb()

    fun playMonetization()

    fun playRevealBombReloaded()

    fun playSwitchAction()
}
