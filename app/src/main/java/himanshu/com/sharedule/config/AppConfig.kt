package himanshu.com.sharedule.config

import android.content.Context
import himanshu.com.sharedule.BuildConfig

object AppConfig {
    private lateinit var configManager: ConfigManager
    
    fun initialize(context: Context) {
        configManager = ConfigManager.getInstance(context)
    }
    
    // Web Client ID from ConfigManager (with fallback to BuildConfig)
    val GOOGLE_WEB_CLIENT_ID: String
        get() = if (::configManager.isInitialized) {
            configManager.googleWebClientId
        } else {
            BuildConfig.GOOGLE_WEB_CLIENT_ID
        }
    
    // Other configuration constants
    val APP_NAME: String
        get() = if (::configManager.isInitialized) {
            configManager.appName
        } else {
            "Sharedule"
        }
    
    val APP_VERSION: String
        get() = if (::configManager.isInitialized) {
            configManager.appVersion
        } else {
            "1.0.0"
        }
} 