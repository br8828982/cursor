package com.iptv.player.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "channels")
data class Channel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val url: String,
    val logoUrl: String? = null,
    val group: String? = null,
    val tvgId: String? = null,
    val tvgName: String? = null,
    val tvgLogo: String? = null,
    val tvgShift: String? = null,
    val radioStation: Boolean = false,
    val userAgent: String? = null,
    val referer: String? = null,
    val headers: Map<String, String>? = null,
    val cookies: String? = null,
    val drmScheme: DrmScheme? = null,
    val drmLicenseUrl: String? = null,
    val drmKeyId: String? = null,
    val drmKey: String? = null,
    val isFavorite: Boolean = false,
    val lastPlayed: Long = 0,
    val playCount: Long = 0
) : Parcelable

enum class DrmScheme {
    WIDEVINE,
    PLAYREADY,
    CLEARKEY,
    NONE
}

@Parcelize
data class DrmConfiguration(
    val scheme: DrmScheme,
    val licenseUrl: String? = null,
    val keyId: String? = null,
    val key: String? = null,
    val headers: Map<String, String>? = null,
    val multiSession: Boolean = false
) : Parcelable
