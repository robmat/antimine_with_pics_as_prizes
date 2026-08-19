package dev.lucasnlm.antimine.core.audio

import android.media.MediaPlayer
import dev.lucasnlm.antimine.preferences.PreferencesRepository

/**
 * [MusicPlayback] implementation backed by [SoundAssetPlayer] - split out of
 * what used to be `GameAudioManagerImpl` directly, since that class's function
 * count was over threshold. Delegated into `GameAudioManagerImpl` via `by`.
 */
class MusicController(
    private val assetPlayer: SoundAssetPlayer,
    private val preferencesRepository: PreferencesRepository,
) : MusicPlayback {
    private var musicMediaPlayer: MediaPlayer? = null

    override fun playMusic() {
        if (preferencesRepository.isMusicEnabled()) {
            if (musicMediaPlayer == null) {
                musicMediaPlayer = assetPlayer.startMusic(GameAudioManagerImpl.MUSIC_FILE_NAME)
            } else {
                musicMediaPlayer?.start()
            }
        } else {
            stopMusic()
        }
    }

    override fun isPlayingMusic(): Boolean {
        return musicMediaPlayer?.isPlaying == true
    }

    override fun pauseMusic() {
        runCatching {
            if (musicMediaPlayer?.isPlaying == true) {
                musicMediaPlayer?.pause()
            }
        }
    }

    override fun resumeMusic() {
        if (preferencesRepository.isMusicEnabled()) {
            if (musicMediaPlayer?.isPlaying == false) {
                musicMediaPlayer?.start()
            }
        }
    }

    override fun stopMusic() {
        musicMediaPlayer?.run {
            stop()
            release()
        }

        musicMediaPlayer = null
    }
}
