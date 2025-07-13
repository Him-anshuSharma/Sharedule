package himanshu.com.sharedule.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import java.text.SimpleDateFormat
import java.util.*

object DeviceInfo {
    
    data class DeviceDetails(
        val deviceName: String,
        val manufacturer: String,
        val model: String,
        val androidVersion: String,
        val apiLevel: Int,
        val deviceId: String,
        val appVersion: String,
        val appVersionCode: Long,
        val lastUpdated: String,
        val screenResolution: String,
        val totalMemory: String,
        val availableMemory: String,
        val isRooted: Boolean,
        val isEmulator: Boolean
    )
    
    fun getDeviceDetails(context: Context): DeviceDetails {
        val packageInfo = getPackageInfo(context)
        val displayMetrics = context.resources.displayMetrics
        
        return DeviceDetails(
            deviceName = Build.DEVICE,
            manufacturer = Build.MANUFACTURER.capitalize(),
            model = Build.MODEL,
            androidVersion = "Android ${Build.VERSION.RELEASE}",
            apiLevel = Build.VERSION.SDK_INT,
            deviceId = getDeviceId(context) ?: "Unknown",
            appVersion = packageInfo.versionName.toString(),
            appVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                packageInfo.versionCode.toLong()
            },
            lastUpdated = formatDate(packageInfo.lastUpdateTime),
            screenResolution = "${displayMetrics.widthPixels}x${displayMetrics.heightPixels}",
            totalMemory = getTotalMemory(),
            availableMemory = getAvailableMemory(),
            isRooted = isDeviceRooted(),
            isEmulator = isEmulator()
        )
    }
    
    private fun getPackageInfo(context: Context): PackageInfo {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            throw RuntimeException("Package not found", e)
        }
    }
    
    private fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "Unknown"
    }
    
    private fun formatDate(timestamp: Long): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
        return formatter.format(date)
    }
    
    private fun getTotalMemory(): String {
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory()
        return formatBytes(totalMemory)
    }
    
    private fun getAvailableMemory(): String {
        val runtime = Runtime.getRuntime()
        val availableMemory = runtime.maxMemory() - runtime.totalMemory() + runtime.freeMemory()
        return formatBytes(availableMemory)
    }
    
    private fun formatBytes(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB")
        var size = bytes.toDouble()
        var unitIndex = 0
        
        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }
        
        return "%.1f %s".format(size, units[unitIndex])
    }
    
    private fun isDeviceRooted(): Boolean {
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true
        }
        
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        
        return paths.any { path ->
            try {
                java.io.File(path).exists()
            } catch (e: Exception) {
                false
            }
        }
    }
    
    private fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT)
    }
    
    fun String.capitalize(): String {
        return if (this.isNotEmpty()) {
            this[0].uppercase() + this.substring(1).lowercase()
        } else {
            this
        }
    }
} 