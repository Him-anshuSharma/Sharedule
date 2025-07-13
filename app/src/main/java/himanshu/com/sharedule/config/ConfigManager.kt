package himanshu.com.sharedule.config

import android.content.Context
import himanshu.com.sharedule.BuildConfig
import java.io.IOException
import java.util.Properties

class ConfigManager private constructor(private val context: Context) {
    
    private val properties = Properties()
    
    init {
        loadConfig()
    }
    
    private fun loadConfig() {
        try {
            context.assets.open("config.properties").use { inputStream ->
                properties.load(inputStream)
            }
        } catch (e: IOException) {
            // Fallback to BuildConfig if properties file is not available
            e.printStackTrace()
        }
    }
    
    fun getString(key: String, defaultValue: String = ""): String {
        return properties.getProperty(key, defaultValue)
    }
    
    fun getInt(key: String, defaultValue: Int = 0): Int {
        return properties.getProperty(key, defaultValue.toString()).toIntOrNull() ?: defaultValue
    }
    
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return properties.getProperty(key, defaultValue.toString()).toBoolean()
    }
    
    companion object {
        @Volatile
        private var INSTANCE: ConfigManager? = null
        
        fun getInstance(context: Context): ConfigManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ConfigManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

// Extension properties for easy access
val ConfigManager.googleWebClientId: String
    get() = getString("google.web.client.id", BuildConfig.GOOGLE_WEB_CLIENT_ID)

val ConfigManager.appName: String
    get() = getString("app.name", "Sharedule")

val ConfigManager.appVersion: String
    get() = getString("app.version", "1.0.0")

val ConfigManager.firebaseProjectId: String
    get() = getString("firebase.project.id", "sharedule-c683c") 