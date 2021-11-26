package com.datn.thesocialnetwork.core.manager

import android.app.Application
import com.datn.thesocialnetwork.core.util.SystemUtils
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * https://ichi.pro/vi/android-exoplayer-phat-video-trong-ung-dung-cua-ban-nhu-youtube-79612915823511
 * https://stackoverflow.com/questions/59439625/how-to-implement-exoplayer-2-11-1-in-android
 */
@Singleton
class PlaybackManager @Inject constructor(
    application: Application,
    private val utils: SystemUtils
) {
    private val mediaSourceFactory: MediaSourceFactory = createMediaSourceFactory(application)
    var isPreparing = false
    var lastPlaybackState: Int? = null
    private val playerEventListener = object : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
//            if (isPreparing && playbackState == ExoPlayer.STATE_READY) {
//                onReady?.invoke()
//                isPreparing = false
//            }else if(!isPreparing && playbackState == ExoPlayer.STATE_ENDED){
//                onEnded?.invoke()
//            }
            if (playbackState != lastPlaybackState) {
                when (playbackState) {
                    ExoPlayer.STATE_READY -> {
                        onReady?.invoke()
                        isPreparing = false
                    }
                    ExoPlayer.STATE_ENDED -> onEnded?.invoke()
                }
                lastPlaybackState = playbackState
            }
        }
    }

    val exoPlayer: SimpleExoPlayer = createPlayer(application).apply { addListener(playerEventListener) }

    private var onReady: (() -> Any?)? = null
    private var onLostFocus: (() -> Any?)? = null
    private var onEnded: (() -> Any?)? = null
    private var currentVolume: Float = 0f

    private fun createPlayer(application: Application): SimpleExoPlayer {
        return SimpleExoPlayer.Builder(application)
            .setBandwidthMeter(DefaultBandwidthMeter.Builder(application).build())
            .setTrackSelector(DefaultTrackSelector(application))
            .build()
            .apply { playWhenReady = false }
    }

    private fun createMediaSourceFactory(application: Application): MediaSourceFactory {
        return ProgressiveMediaSource.Factory(
            CacheDataSourceFactory(application, 100 * 1024 * 1024, 10 * 1024 * 1024),
            DefaultExtractorsFactory()
        )
    }

    fun prepare(uri: String, doLooping: Boolean, onReady: () -> Any?, onLostFocus: (() -> Any?)? = null, onEnded: (() -> Any?)? = null) {
        pause()

        try {
            this.onLostFocus?.invoke()
        } catch (t: Throwable) {
            t.printStackTrace()
        }

        try {
            var mediaSource = mediaSourceFactory.createMediaSource(utils.getUri(uri))
            if (doLooping) {
                mediaSource = LoopingMediaSource(mediaSource)
            }

            this.onReady = onReady
            this.onLostFocus = onLostFocus
            this.onEnded = onEnded

            isPreparing = true
            exoPlayer.prepare(mediaSource)
        } catch (t: Throwable) {
            t.printStackTrace()
            isPreparing = false
        }
    }

    fun resume() {
        exoPlayer.playWhenReady = true
    }

    fun pause() {
        exoPlayer.playWhenReady = false
    }

    fun mute() {
        if (exoPlayer.volume > 0) {
            currentVolume = exoPlayer.volume
            exoPlayer.volume = 0f
        }
    }

    fun unMute() {
        if (exoPlayer.volume == 0f) {
            exoPlayer.volume = currentVolume
        }
    }

    fun isMuted(): Boolean {
        return exoPlayer.volume == 0f
    }
}