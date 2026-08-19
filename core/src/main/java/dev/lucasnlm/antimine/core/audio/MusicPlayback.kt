package dev.lucasnlm.antimine.core.audio

/** The background-music half of [GameAudioManager]'s contract. */
interface MusicPlayback {
    fun playMusic()

    fun isPlayingMusic(): Boolean

    fun pauseMusic()

    fun resumeMusic()

    fun stopMusic()
}
