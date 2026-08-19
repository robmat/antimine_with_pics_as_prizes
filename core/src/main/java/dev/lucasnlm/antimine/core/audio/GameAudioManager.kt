package dev.lucasnlm.antimine.core.audio

interface GameAudioManager : MusicPlayback, SoundEffects {
    fun free()

    fun getComposerData(): List<ComposerData>
}
