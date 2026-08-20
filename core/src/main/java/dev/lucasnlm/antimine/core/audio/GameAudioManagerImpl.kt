package dev.lucasnlm.antimine.core.audio

import android.content.Context
import dev.lucasnlm.antimine.preferences.PreferencesRepository

class GameAudioManagerImpl(
    context: Context,
    preferencesRepository: PreferencesRepository,
) : GameAudioManager,
    MusicPlayback by MusicController(SoundAssetPlayer(context, preferencesRepository), preferencesRepository),
    SoundEffects by SoundEffectPlayerImpl(SoundAssetPlayer(context, preferencesRepository)) {
    override fun free() {
        stopMusic()
    }

    override fun getComposerData(): List<ComposerData> =
        listOf(
            ComposerData(
                composer = "Tatyana Jacques",
                composerLink = "https://open.spotify.com/artist/5Z1PXKko20wSH0yFr9HtNr",
            ),
        )

    companion object {
        private fun filesCount(count: Int) = (0 until count)

        private const val OPEN_AREA_COUNT = 4
        private const val OPEN_MULTIPLE_COUNT = 3
        private const val PUT_FLAG_COUNT = 3
        private const val REVEAL_BOMB_COUNT = 3

        const val MUSIC_FILE_NAME = "music.ogg"
        const val WIN_FILE_NAME = "win.ogg"
        const val BOMB_EXPLOSION_FILE_NAME = "bomb_explosion.ogg"
        const val REVEAL_BOMB_RELOAD_FILE_NAME = "reveal_mine_reload.ogg"

        fun clickFileName() = listOf("menu_click.ogg", "menu_click_alt.ogg", "menu_click_back.ogg")

        fun openAreaFiles() = filesCount(OPEN_AREA_COUNT).map { "open_area_$it.ogg" }

        fun openMultipleFiles() = filesCount(OPEN_MULTIPLE_COUNT).map { "open_multiple_$it.ogg" }

        fun putFlagFiles() = filesCount(PUT_FLAG_COUNT).map { "put_flag_$it.ogg" }

        fun revealBombFiles() = filesCount(REVEAL_BOMB_COUNT).map { "reveal_mine_$it.ogg" }
    }
}
