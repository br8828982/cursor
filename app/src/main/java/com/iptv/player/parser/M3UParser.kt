package com.iptv.player.parser

import android.util.Log
import com.iptv.player.data.model.Channel
import com.iptv.player.data.model.DrmScheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class M3UParser @Inject constructor() {
    
    suspend fun parseM3U(inputStream: InputStream): List<Channel> = withContext(Dispatchers.IO) {
        val channels = mutableListOf<Channel>()
        val reader = BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))
        
        var currentExtInf: String? = null
        var currentAttributes = mutableMapOf<String, String>()
        
        try {
            reader.useLines { lines ->
                for (line in lines) {
                    val trimmedLine = line.trim()
                    
                    when {
                        trimmedLine.startsWith("#EXTM3U") -> {
                            // Parse M3U header
                            parseM3UHeader(trimmedLine)
                        }
                        
                        trimmedLine.startsWith("#EXTINF:") -> {
                            currentExtInf = trimmedLine
                            currentAttributes.clear()
                        }
                        
                        trimmedLine.startsWith("#EXTVLCOPT:") -> {
                            parseVLCOption(trimmedLine, currentAttributes)
                        }
                        
                        trimmedLine.startsWith("#KODIPROP:") -> {
                            parseKodiProperty(trimmedLine, currentAttributes)
                        }
                        
                        trimmedLine.startsWith("#EXT-X-") -> {
                            // Parse extended attributes
                            parseExtendedAttribute(trimmedLine, currentAttributes)
                        }
                        
                        trimmedLine.startsWith("http") || trimmedLine.startsWith("https") || 
                        trimmedLine.startsWith("rtmp") || trimmedLine.startsWith("rtsp") -> {
                            // This is a URL line
                            currentExtInf?.let { extinf ->
                                val channel = parseChannel(extinf, trimmedLine, currentAttributes)
                                if (channel != null) {
                                    channels.add(channel)
                                }
                            }
                            currentExtInf = null
                            currentAttributes.clear()
                        }
                        
                        trimmedLine.isNotEmpty() && !trimmedLine.startsWith("#") -> {
                            // Fallback for other URLs
                            currentExtInf?.let { extinf ->
                                val channel = parseChannel(extinf, trimmedLine, currentAttributes)
                                if (channel != null) {
                                    channels.add(channel)
                                }
                            }
                            currentExtInf = null
                            currentAttributes.clear()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing M3U", e)
        }
        
        channels
    }
    
    private fun parseM3UHeader(line: String) {
        // Parse any global M3U attributes if needed
        Log.d(TAG, "Parsing M3U header: $line")
    }
    
    private fun parseVLCOption(line: String, attributes: MutableMap<String, String>) {
        // #EXTVLCOPT:http-user-agent=Mozilla/5.0
        // #EXTVLCOPT:http-referrer=https://example.com
        val optionPart = line.substringAfter("#EXTVLCOPT:")
        val equalIndex = optionPart.indexOf('=')
        
        if (equalIndex > 0) {
            val key = optionPart.substring(0, equalIndex).trim()
            val value = optionPart.substring(equalIndex + 1).trim()
            
            when (key) {
                "http-user-agent" -> attributes["user-agent"] = value
                "http-referrer" -> attributes["referer"] = value
                "http-cookie" -> attributes["cookie"] = value
                else -> attributes[key] = value
            }
        }
    }
    
    private fun parseKodiProperty(line: String, attributes: MutableMap<String, String>) {
        // #KODIPROP:inputstream.adaptive.license_type=com.widevine.alpha
        // #KODIPROP:inputstream.adaptive.license_key=https://license.url.here
        val propPart = line.substringAfter("#KODIPROP:")
        val equalIndex = propPart.indexOf('=')
        
        if (equalIndex > 0) {
            val key = propPart.substring(0, equalIndex).trim()
            val value = propPart.substring(equalIndex + 1).trim()
            
            when (key) {
                "inputstream.adaptive.license_type" -> {
                    attributes["drm-scheme"] = when (value) {
                        "com.widevine.alpha" -> "widevine"
                        "com.microsoft.playready" -> "playready"
                        "org.w3.clearkey" -> "clearkey"
                        else -> value
                    }
                }
                "inputstream.adaptive.license_key" -> attributes["drm-license-url"] = value
                "inputstream.adaptive.license_data" -> attributes["drm-license-data"] = value
                else -> attributes[key] = value
            }
        }
    }
    
    private fun parseExtendedAttribute(line: String, attributes: MutableMap<String, String>) {
        // Parse other extended M3U8 attributes
        if (line.contains("=")) {
            val parts = line.split("=", limit = 2)
            if (parts.size == 2) {
                attributes[parts[0].trim()] = parts[1].trim()
            }
        }
    }
    
    private fun parseChannel(extinf: String, url: String, attributes: Map<String, String>): Channel? {
        return try {
            val extinfPart = extinf.substringAfter("#EXTINF:")
            val commaIndex = extinfPart.indexOf(',')
            
            if (commaIndex == -1) return null
            
            val duration = extinfPart.substring(0, commaIndex).trim()
            val nameAndAttributes = extinfPart.substring(commaIndex + 1).trim()
            
            // Parse channel attributes from EXTINF line
            val channelAttributes = parseExtinfAttributes(duration)
            val channelName = extractChannelName(nameAndAttributes)
            
            // Combine attributes from both sources
            val allAttributes = channelAttributes + attributes
            
            Channel(
                name = channelName,
                url = url,
                logoUrl = allAttributes["tvg-logo"],
                group = allAttributes["group-title"],
                tvgId = allAttributes["tvg-id"],
                tvgName = allAttributes["tvg-name"],
                tvgLogo = allAttributes["tvg-logo"],
                tvgShift = allAttributes["tvg-shift"],
                radioStation = allAttributes["radio"]?.toBoolean() ?: false,
                userAgent = allAttributes["user-agent"],
                referer = allAttributes["referer"],
                headers = buildHeaders(allAttributes),
                cookies = allAttributes["cookie"],
                drmScheme = parseDrmScheme(allAttributes["drm-scheme"]),
                drmLicenseUrl = allAttributes["drm-license-url"],
                drmKeyId = allAttributes["drm-key-id"],
                drmKey = allAttributes["drm-key"]
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing channel: $extinf -> $url", e)
            null
        }
    }
    
    private fun parseExtinfAttributes(duration: String): Map<String, String> {
        val attributes = mutableMapOf<String, String>()
        
        // Parse attributes from the duration part (e.g., -1 tvg-id="1" tvg-name="Channel" tvg-logo="logo.png" group-title="Entertainment")
        val regex = """(\w+(?:-\w+)*)="([^"]*)"""".toRegex()
        val matches = regex.findAll(duration)
        
        for (match in matches) {
            val key = match.groupValues[1]
            val value = match.groupValues[2]
            attributes[key] = value
        }
        
        return attributes
    }
    
    private fun extractChannelName(nameAndAttributes: String): String {
        // Remove attributes and get clean channel name
        var name = nameAndAttributes
        
        // Remove attributes like tvg-id="1" tvg-name="Channel"
        name = name.replace("""tvg-\w+="[^"]*"""".toRegex(), "")
        name = name.replace("""group-title="[^"]*"""".toRegex(), "")
        name = name.replace("""\s+""".toRegex(), " ")
        
        return name.trim()
    }
    
    private fun buildHeaders(attributes: Map<String, String>): Map<String, String>? {
        val headers = mutableMapOf<String, String>()
        
        attributes["user-agent"]?.let { headers["User-Agent"] = it }
        attributes["referer"]?.let { headers["Referer"] = it }
        attributes["cookie"]?.let { headers["Cookie"] = it }
        
        // Add any custom headers
        attributes.filter { it.key.startsWith("header-") }
            .forEach { (key, value) ->
                val headerName = key.substringAfter("header-")
                    .split("-")
                    .joinToString("-") { part ->
                        part.lowercase().replaceFirstChar { it.uppercase() }
                    }
                headers[headerName] = value
            }
        
        return if (headers.isNotEmpty()) headers else null
    }
    
    private fun parseDrmScheme(scheme: String?): DrmScheme? {
        return when (scheme?.lowercase()) {
            "widevine", "com.widevine.alpha" -> DrmScheme.WIDEVINE
            "playready", "com.microsoft.playready" -> DrmScheme.PLAYREADY
            "clearkey", "org.w3.clearkey" -> DrmScheme.CLEARKEY
            "none" -> DrmScheme.NONE
            null -> null
            else -> {
                Log.w(TAG, "Unknown DRM scheme: $scheme")
                null
            }
        }
    }
    
    companion object {
        private const val TAG = "M3UParser"
    }
}
