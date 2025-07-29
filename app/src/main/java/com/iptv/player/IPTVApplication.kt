package com.iptv.player

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class IPTVApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
    
    companion object {
        lateinit var instance: IPTVApplication
            private set
    }
}
