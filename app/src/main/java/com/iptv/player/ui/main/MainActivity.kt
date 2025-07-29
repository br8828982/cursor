package com.iptv.player.ui.main

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.iptv.player.ui.channels.ChannelsScreen
import com.iptv.player.ui.player.PlayerActivity
import com.iptv.player.ui.playlists.PlaylistsScreen
import com.iptv.player.ui.settings.SettingsScreen
import com.iptv.player.ui.theme.IPTVPlayerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { handlePlaylistImport(it) }
    }
    
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            IPTVPlayerTheme {
                val navController = rememberNavController()
                
                // Request necessary permissions
                val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    listOf(
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.READ_MEDIA_AUDIO
                    )
                } else {
                    listOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                }
                
                val permissionsState = rememberMultiplePermissionsState(permissions)
                
                LaunchedEffect(Unit) {
                    if (!permissionsState.allPermissionsGranted) {
                        permissionsState.launchMultiplePermissionRequest()
                    }
                }
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = "main"
                    ) {
                        composable("main") {
                            MainScreen(
                                navController = navController,
                                onPlayChannel = { channel ->
                                    val intent = Intent(this@MainActivity, PlayerActivity::class.java).apply {
                                        putExtra("channel", channel)
                                    }
                                    startActivity(intent)
                                },
                                onImportPlaylist = {
                                    filePickerLauncher.launch("*/*")
                                }
                            )
                        }
                        
                        composable("channels") {
                            ChannelsScreen(
                                onBackClick = { navController.popBackStack() },
                                onPlayChannel = { channel ->
                                    val intent = Intent(this@MainActivity, PlayerActivity::class.java).apply {
                                        putExtra("channel", channel)
                                    }
                                    startActivity(intent)
                                }
                            )
                        }
                        
                        composable("playlists") {
                            PlaylistsScreen(
                                onBackClick = { navController.popBackStack() },
                                onImportPlaylist = {
                                    filePickerLauncher.launch("*/*")
                                }
                            )
                        }
                        
                        composable("settings") {
                            SettingsScreen(
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
        
        // Handle intent data (M3U files opened with the app)
        handleIntent(intent)
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleIntent(it) }
    }
    
    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_VIEW -> {
                intent.data?.let { uri ->
                    handlePlaylistImport(uri)
                }
            }
        }
    }
    
    private fun handlePlaylistImport(uri: Uri) {
        // This will be handled by the MainViewModel
        // For now, we'll just log it
        println("Importing playlist from: $uri")
    }
}
