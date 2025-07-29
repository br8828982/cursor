package com.iptv.player.player

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.*
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.*
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.smoothstreaming.SsMediaSource
import androidx.media3.exoplayer.drm.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import com.iptv.player.data.model.Channel
import com.iptv.player.data.model.DrmScheme
import java.util.*

@UnstableApi
@Singleton
class IPTVExoPlayer @Inject constructor(
    private val context: Context
) {
    private var player: ExoPlayer? = null
    private var currentChannel: Channel? = null
    
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    fun initializePlayer(): ExoPlayer {
        releasePlayer()
        
        val renderersFactory = DefaultRenderersFactory(context)
            .setEnableDecoderFallback(true)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
        
        player = ExoPlayer.Builder(context, renderersFactory)
            .setMediaSourceFactory(createMediaSourceFactory())
            .setLoadControl(createLoadControl())
            .build()
        
        return player!!
    }
    
    private fun createMediaSourceFactory(): MediaSourceFactory {
        val dataSourceFactory = createDataSourceFactory()
        
        return DefaultMediaSourceFactory(dataSourceFactory)
            .setDrmSessionManagerProvider { mediaItem ->
                createDrmSessionManager(mediaItem)
            }
    }
    
    private fun createDataSourceFactory(): DataSource.Factory {
        val httpDataSourceFactory = OkHttpDataSource.Factory(okHttpClient)
            .setUserAgent(getUserAgent())
        
        return DefaultDataSourceFactory(
            context,
            httpDataSourceFactory
        )
    }
    
    private fun createLoadControl(): LoadControl {
        return DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
                DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS,
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
            )
            .build()
    }
    
    private fun createDrmSessionManager(mediaItem: MediaItem): DrmSessionManager {
        return when (currentChannel?.drmScheme) {
            DrmScheme.WIDEVINE -> createWidevineDrmSessionManager(mediaItem)
            DrmScheme.PLAYREADY -> createPlayReadyDrmSessionManager(mediaItem)
            DrmScheme.CLEARKEY -> createClearKeyDrmSessionManager(mediaItem)
            else -> DrmSessionManager.DRM_UNSUPPORTED
        }
    }
    
    private fun createWidevineDrmSessionManager(mediaItem: MediaItem): DrmSessionManager {
        val channel = currentChannel ?: return DrmSessionManager.DRM_UNSUPPORTED
        
        val httpDataSourceFactory = createHttpDataSourceFactory(channel)
        
        return DefaultDrmSessionManager.Builder()
            .setUuidAndExoMediaDrmProvider(
                C.WIDEVINE_UUID,
                FrameworkMediaDrm.DEFAULT_PROVIDER
            )
            .build(httpDataSourceFactory)
    }
    
    private fun createPlayReadyDrmSessionManager(mediaItem: MediaItem): DrmSessionManager {
        val channel = currentChannel ?: return DrmSessionManager.DRM_UNSUPPORTED
        
        val httpDataSourceFactory = createHttpDataSourceFactory(channel)
        
        return DefaultDrmSessionManager.Builder()
            .setUuidAndExoMediaDrmProvider(
                C.PLAYREADY_UUID,
                FrameworkMediaDrm.DEFAULT_PROVIDER
            )
            .build(httpDataSourceFactory)
    }
    
    private fun createClearKeyDrmSessionManager(mediaItem: MediaItem): DrmSessionManager {
        val channel = currentChannel ?: return DrmSessionManager.DRM_UNSUPPORTED
        
        if (channel.drmKeyId != null && channel.drmKey != null) {
            val clearKeyData = mapOf(
                channel.drmKeyId to channel.drmKey
            )
            
            return LocalMediaDrmCallback(clearKeyData.toByteArray())
                .let { callback ->
                    DefaultDrmSessionManager.Builder()
                        .setUuidAndExoMediaDrmProvider(
                            C.CLEARKEY_UUID,
                            FrameworkMediaDrm.DEFAULT_PROVIDER
                        )
                        .build(callback)
                }
        }
        
        return DrmSessionManager.DRM_UNSUPPORTED
    }
    
    private fun createHttpDataSourceFactory(channel: Channel): HttpDataSource.Factory {
        val factory = OkHttpDataSource.Factory(okHttpClient)
            .setUserAgent(channel.userAgent ?: getUserAgent())
        
        // Add custom headers
        channel.headers?.let { headers ->
            factory.setDefaultRequestProperties(headers)
        }
        
        // Add referer
        channel.referer?.let { referer ->
            factory.setDefaultRequestProperties(mapOf("Referer" to referer))
        }
        
        // Add cookies
        channel.cookies?.let { cookies ->
            factory.setDefaultRequestProperties(mapOf("Cookie" to cookies))
        }
        
        return factory
    }
    
    suspend fun playChannel(channel: Channel) = withContext(Dispatchers.Main) {
        currentChannel = channel
        
        val mediaItem = createMediaItem(channel)
        player?.apply {
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
    }
    
    private fun createMediaItem(channel: Channel): MediaItem {
        val builder = MediaItem.Builder()
            .setUri(channel.url)
            .setMediaId(channel.id.toString())
        
        // Add DRM configuration if available
        if (channel.drmScheme != null && channel.drmScheme != DrmScheme.NONE) {
            val drmConfiguration = MediaItem.DrmConfiguration.Builder(
                when (channel.drmScheme) {
                    DrmScheme.WIDEVINE -> C.WIDEVINE_UUID
                    DrmScheme.PLAYREADY -> C.PLAYREADY_UUID
                    DrmScheme.CLEARKEY -> C.CLEARKEY_UUID
                    else -> C.WIDEVINE_UUID
                }
            )
            
            channel.drmLicenseUrl?.let { licenseUrl ->
                drmConfiguration.setLicenseUri(licenseUrl)
            }
            
            // Add DRM headers
            channel.headers?.let { headers ->
                drmConfiguration.setLicenseRequestHeaders(headers)
            }
            
            builder.setDrmConfiguration(drmConfiguration.build())
        }
        
        return builder.build()
    }
    
    private fun getUserAgent(): String {
        return "IPTVPlayer/1.0 (Android)"
    }
    
    fun getPlayer(): ExoPlayer? = player
    
    fun releasePlayer() {
        player?.release()
        player = null
        currentChannel = null
    }
    
    fun isPlaying(): Boolean = player?.isPlaying == true
    
    fun pause() {
        player?.pause()
    }
    
    fun resume() {
        player?.play()
    }
    
    fun seekTo(positionMs: Long) {
        player?.seekTo(positionMs)
    }
    
    fun getCurrentPosition(): Long = player?.currentPosition ?: 0L
    
    fun getDuration(): Long = player?.duration ?: C.TIME_UNSET
    
    companion object {
        private const val TAG = "IPTVExoPlayer"
    }
}

// Helper class for ClearKey DRM
private class LocalMediaDrmCallback(
    private val keyData: ByteArray
) : MediaDrmCallback {
    
    override fun executeProvisionRequest(
        uuid: UUID,
        request: ExoMediaDrm.ProvisionRequest
    ): ByteArray {
        throw UnsupportedOperationException("Provision requests are not supported.")
    }
    
    override fun executeKeyRequest(
        uuid: UUID,
        request: ExoMediaDrm.KeyRequest
    ): ByteArray {
        return keyData
    }
}

private fun Map<String, String>.toByteArray(): ByteArray {
    // Convert key-value pairs to JSON format for ClearKey
    val json = entries.joinToString(",", "{", "}") { 
        "\"${it.key}\":\"${it.value}\"" 
    }
    return json.toByteArray()
}
