package dev.lucasnlm.antimine.core.audio

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import dev.lucasnlm.antimine.preferences.PreferencesRepository

/**
 * Low-level asset-backed [MediaPlayer] playback shared by [MusicController] and
 * [SoundEffectPlayerImpl] - split out of what used to be `GameAudioManagerImpl`
 * directly, since that class's function count was over threshold.
 */
class SoundAssetPlayer(
    private val context: Context,
    private val preferencesRepository: PreferencesRepository,
) {
    companion object {
        const val MUSIC_MAX_VOLUME = 0.3f
        const val SFX_MAX_VOLUME = 0.7f
    }

    private data class MediaPlayerRequest(
        val soundAsset: AssetFileDescriptor,
        val volume: Float,
        val repeat: Boolean,
        val releaseOnComplete: Boolean,
        val isMusic: Boolean = false,
        val seekTo: Int? = null,
    )

    /** Plays a one-shot sound effect if the user has sound effects enabled. */
    fun playSfx(fileName: String) {
        if (preferencesRepository.isSoundEffectsEnabled()) {
            tryOpenFd(fileName)?.use { soundAsset ->
                playWithMediaPlayer(
                    MediaPlayerRequest(
                        soundAsset = soundAsset,
                        volume = SFX_MAX_VOLUME,
                        repeat = false,
                        releaseOnComplete = true,
                        seekTo = 0,
                        isMusic = false,
                    ),
                )
            }
        }
    }

    /** Plays a random one of [fileNames] as a one-shot sound effect. */
    fun playSfx(fileNames: List<String>) {
        fileNames.shuffled().firstOrNull()?.let(::playSfx)
    }

    /** Opens [fileName] as a looping, non-releasing background-music track. */
    fun startMusic(fileName: String): MediaPlayer? {
        return tryOpenFd(fileName)?.use { musicFd ->
            playWithMediaPlayer(
                MediaPlayerRequest(
                    soundAsset = musicFd,
                    volume = MUSIC_MAX_VOLUME,
                    repeat = true,
                    releaseOnComplete = false,
                    isMusic = true,
                ),
            )
        }
    }

    private fun getAudioAttributes(isMusic: Boolean): AudioAttributes {
        return AudioAttributes.Builder().apply {
            setUsage(AudioAttributes.USAGE_GAME)

            if (isMusic) {
                setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            } else {
                setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (isMusic) {
                    setAllowedCapturePolicy(AudioAttributes.ALLOW_CAPTURE_BY_NONE)
                } else {
                    setAllowedCapturePolicy(AudioAttributes.ALLOW_CAPTURE_BY_ALL)
                }
            }
        }.build()
    }

    private fun playWithMediaPlayer(request: MediaPlayerRequest): MediaPlayer {
        val mediaPlayer = MediaPlayer()
        runCatching {
            mediaPlayer.run {
                val soundAsset = request.soundAsset
                setDataSource(soundAsset.fileDescriptor, soundAsset.startOffset, soundAsset.length)
                prepare()
                setAudioAttributes(getAudioAttributes(request.isMusic))
                setVolume(request.volume, request.volume)
                request.seekTo?.let(::seekTo)
                isLooping = request.repeat
                if (request.releaseOnComplete) {
                    setOnCompletionListener {
                        release()
                    }
                }
                start()
            }
        }.onFailure {
            mediaPlayer.release()
        }
        return mediaPlayer
    }

    private fun tryOpenFd(fileName: String): AssetFileDescriptor? {
        return runCatching {
            context.assets.openFd(fileName)
        }.getOrNull()
    }
}
