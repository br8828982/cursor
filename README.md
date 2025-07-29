# IPTV Player - Modern Android IPTV Streaming App

A powerful, feature-rich Android IPTV player built with the latest technologies including ExoPlayer, Jetpack Compose, and DRM support.

## ✨ Features

### 🎥 **Advanced Video Playback**
- **ExoPlayer Integration**: Latest Media3 ExoPlayer for optimal performance
- **All Video Codecs**: Support for H.264, H.265/HEVC, VP8, VP9, AV1
- **Adaptive Streaming**: HLS, DASH, SmoothStreaming support
- **Live & VOD**: Both live streams and video-on-demand content

### 🔐 **DRM Protection**
- **Widevine DRM**: Full Widevine L1/L3 support
- **PlayReady DRM**: Microsoft PlayReady integration
- **ClearKey DRM**: Local and remote ClearKey support
- **Custom Keys**: Support for offline kid keys

### 🌐 **Network & Authentication**
- **Custom Headers**: Inject any HTTP headers
- **Cookie Support**: Full cookie authentication
- **User Agent**: Custom user agent strings
- **Referer Support**: HTTP referer header support
- **OkHttp Integration**: Advanced networking with connection pooling

### 📺 **Playlist Management**
- **M3U/M3U8 Support**: Complete M3U playlist parsing
- **Multiple Sources**: URL, local files, content URIs
- **Auto-Update**: Automatic playlist refresh
- **Groups & Categories**: Channel organization
- **Favorites**: Personal channel favorites

### 🎨 **Modern UI/UX**
- **Material Design 3**: Latest Material You design
- **Jetpack Compose**: 100% Compose UI
- **Dark/Light Theme**: System theme support
- **Responsive Design**: TV and mobile optimized
- **Animations**: Smooth transitions and animations

### 📱 **Platform Support**
- **Android TV**: Full Android TV support
- **Mobile**: Phone and tablet support
- **Landscape Mode**: Optimized for landscape viewing
- **Fullscreen**: Immersive fullscreen experience

## 🏗️ **Architecture**

This app follows modern Android development best practices:

- **MVVM Architecture**: Clear separation of concerns
- **Dependency Injection**: Hilt for DI
- **Repository Pattern**: Clean data layer
- **Room Database**: Local data persistence
- **Coroutines & Flow**: Reactive programming
- **Single Activity**: Navigation with Compose

## 🛠️ **Tech Stack**

### **Core Technologies**
- **Kotlin**: 100% Kotlin codebase
- **Jetpack Compose**: Modern UI toolkit
- **ExoPlayer (Media3)**: Advanced video playback
- **Hilt**: Dependency injection
- **Room**: Local database
- **Navigation Compose**: Type-safe navigation

### **Media & Networking**
- **OkHttp**: HTTP client with interceptors
- **Retrofit**: REST API client
- **Gson**: JSON parsing
- **Coil**: Image loading for Compose

### **DRM & Security**
- **Widevine**: Google's DRM solution
- **PlayReady**: Microsoft's DRM solution
- **ClearKey**: W3C clear key standard

## 📦 **Installation**

### **Prerequisites**
- Android Studio Arctic Fox or later
- Android SDK 24+
- Kotlin 1.9+

### **Build Instructions**

1. **Clone the repository**:
```bash
git clone https://github.com/yourusername/iptv-player.git
cd iptv-player
```

2. **Open in Android Studio**:
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory

3. **Build and Run**:
   - Sync project with Gradle files
   - Run the app on device or emulator

## 🚀 **Getting Started**

### **Import a Playlist**

1. **From URL**:
   - Tap "Import" on main screen
   - Enter M3U/M3U8 URL
   - Add custom headers if needed

2. **From File**:
   - Use device file picker
   - Select local M3U file
   - Channels will be imported automatically

### **Supported M3U Format**

```m3u
#EXTM3U
#EXTINF:-1 tvg-id="1" tvg-name="Channel 1" tvg-logo="http://example.com/logo.png" group-title="Entertainment",Channel 1
#EXTVLCOPT:http-user-agent=MyAgent/1.0
#KODIPROP:inputstream.adaptive.license_type=com.widevine.alpha
#KODIPROP:inputstream.adaptive.license_key=https://license.server.com/license
http://example.com/channel1.m3u8

#EXTINF:-1 tvg-id="2" tvg-name="Channel 2" group-title="News",Channel 2
http://example.com/channel2.m3u8
```

### **DRM Configuration**

The app supports multiple DRM schemes:

```m3u
#KODIPROP:inputstream.adaptive.license_type=com.widevine.alpha
#KODIPROP:inputstream.adaptive.license_key=https://drm.server.com/license
```

For ClearKey:
```m3u
#KODIPROP:inputstream.adaptive.license_type=org.w3.clearkey
#KODIPROP:inputstream.adaptive.license_data={"keys":[{"kty":"oct","k":"KEY_DATA","kid":"KEY_ID"}]}
```

## 🔧 **Configuration**

### **Custom Headers**
Add authentication headers to your M3U:

```m3u
#EXTVLCOPT:http-user-agent=CustomAgent/1.0
#EXTVLCOPT:http-referrer=https://example.com
#EXTVLCOPT:http-cookie=session=abc123; auth=xyz789
```

### **Network Settings**
- Connection timeout: 30 seconds
- Read timeout: 30 seconds
- Automatic retries on failure
- Connection pooling for efficiency

## 📱 **Usage**

### **Main Features**

1. **Channel Browsing**:
   - Browse all channels
   - Filter by groups
   - Search channels
   - Mark favorites

2. **Playback Controls**:
   - Play/Pause
   - Fullscreen mode
   - Auto-hide controls
   - Error handling with retry

3. **Playlist Management**:
   - Import from URL/file
   - Auto-update playlists
   - Delete playlists
   - View channel count

## 🎯 **Key Advantages**

### **vs OTT Navigator**
- **Modern UI**: Clean, Material Design 3 interface
- **Better Performance**: Optimized with latest technologies
- **DRM Support**: Comprehensive DRM implementation
- **Open Source**: Fully open and customizable
- **No Ads**: Clean, ad-free experience

### **vs Other Players**
- **Latest ExoPlayer**: Most advanced video engine
- **Compose UI**: Smooth, responsive interface
- **Full Codec Support**: All video/audio formats
- **Custom Authentication**: Flexible header/cookie support
- **TV Optimized**: Great Android TV experience

## 🔒 **Privacy & Security**

- **No Data Collection**: App doesn't collect personal data
- **Local Storage**: All data stored locally
- **DRM Compliance**: Industry-standard DRM support
- **Secure Networking**: HTTPS enforcement
- **Permission Minimal**: Only required permissions

## 🐛 **Troubleshooting**

### **Common Issues**

1. **Playback Errors**:
   - Check network connection
   - Verify stream URL
   - Check DRM configuration

2. **Import Failures**:
   - Verify M3U format
   - Check file permissions
   - Ensure valid URLs

3. **DRM Issues**:
   - Verify license server
   - Check device DRM support
   - Validate key format

## 🤝 **Contributing**

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## 📄 **License**

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 **Acknowledgments**

- **Google ExoPlayer Team**: For the excellent media library
- **Jetpack Compose Team**: For the modern UI toolkit
- **Android Community**: For continuous innovation
- **Contributors**: All developers who contribute to this project

## 📞 **Support**

For support and questions:
- Create an issue on GitHub
- Check the documentation
- Review troubleshooting guide

---

**Built with ❤️ for the Android IPTV community**
