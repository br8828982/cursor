package com.iptv.player.ui.player

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.media3.common.util.UnstableApi
import com.iptv.player.data.model.Channel
import com.iptv.player.ui.theme.IPTVPlayerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
@UnstableApi
class PlayerActivity : ComponentActivity() {
    
    private val viewModel: PlayerViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup fullscreen
        WindowCompat.setDecorFitsSystemWindows(window, false)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        
        // Get channel from intent
        val channel = intent.getParcelableExtra<Channel>("channel")
        
        if (channel == null) {
            finish()
            return
        }
        
        setContent {
            IPTVPlayerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PlayerScreen(
                        channel = channel,
                        viewModel = viewModel,
                        onBackPressed = { finish() }
                    )
                }
            }
        }
        
        // Initialize player with channel
        viewModel.initializePlayer(channel)
    }
    
    override fun onResume() {
        super.onResume()
        hideSystemUI()
        viewModel.resumePlayer()
    }
    
    override fun onPause() {
        super.onPause()
        viewModel.pausePlayer()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        viewModel.releasePlayer()
    }
    
    private fun hideSystemUI() {
        window.decorView.apply {
            systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
            )
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }
}
