package com.iptv.player.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val url: String? = null,
    val filePath: String? = null,
    val isLocal: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis(),
    val channelCount: Int = 0,
    val userAgent: String? = null,
    val referer: String? = null,
    val headers: Map<String, String>? = null,
    val autoUpdate: Boolean = false,
    val updateInterval: Long = 24 * 60 * 60 * 1000L // 24 hours
) : Parcelable

@Parcelize
data class M3UInfo(
    val extm3u: Boolean = false,
    val version: String? = null,
    val attributes: Map<String, String> = emptyMap()
) : Parcelable
