package dev.lucasnlm.antimine.core.audio

/**
 * [SoundEffects] implementation backed by [SoundAssetPlayer] - split out of
 * what used to be `GameAudioManagerImpl` directly, since that class's function
 * count was over threshold. Delegated into `GameAudioManagerImpl` via `by`.
 */
class SoundEffectPlayerImpl(
    private val assetPlayer: SoundAssetPlayer,
) : SoundEffects {
    override fun playWin() {
        assetPlayer.playSfx(GameAudioManagerImpl.WIN_FILE_NAME)
    }

    override fun playBombExplosion() {
        assetPlayer.playSfx(GameAudioManagerImpl.BOMB_EXPLOSION_FILE_NAME)
    }

    override fun playClickSound(index: Int) {
        GameAudioManagerImpl.clickFileName().getOrNull(index)?.let(assetPlayer::playSfx)
    }

    override fun playOpenArea() {
        assetPlayer.playSfx(GameAudioManagerImpl.openAreaFiles())
    }

    override fun playPutFlag() {
        assetPlayer.playSfx(GameAudioManagerImpl.putFlagFiles())
    }

    override fun playOpenMultipleArea() {
        assetPlayer.playSfx(GameAudioManagerImpl.openMultipleFiles())
    }

    override fun playRevealBomb() {
        assetPlayer.playSfx(GameAudioManagerImpl.revealBombFiles())
    }

    override fun playMonetization() {
        assetPlayer.playSfx(GameAudioManagerImpl.revealBombFiles())
    }

    override fun playRevealBombReloaded() {
        assetPlayer.playSfx(GameAudioManagerImpl.REVEAL_BOMB_RELOAD_FILE_NAME)
    }

    override fun playSwitchAction() {
        assetPlayer.playSfx(GameAudioManagerImpl.REVEAL_BOMB_RELOAD_FILE_NAME)
    }
}
