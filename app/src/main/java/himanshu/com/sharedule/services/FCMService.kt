package himanshu.com.sharedule.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import himanshu.com.sharedule.MainActivity
import himanshu.com.sharedule.R

class FCMService : FirebaseMessagingService() {

    object NotificationStore {
        val notifications = mutableListOf<String>()
    }


    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "sharedule_notifications"
        private const val CHANNEL_NAME = "Sharedule Notifications"
        private const val CHANNEL_DESCRIPTION = "Notifications for Sharedule app"
        private const val NOTIFICATION_ID = 1
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        
        // TODO: Send this token to your server
        sendTokenToServer(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "Message received from: ${remoteMessage.from}")
        
        // Check if message contains a data payload
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
        }

        // Check if message contains a notification payload
        remoteMessage.notification?.let {
            Log.d(TAG, "Message notification body: ${it.body}")
            Log.d(TAG, "Message notification title: ${it.title}")
        }

        // Handle the message and show notification
        handleMessage(remoteMessage)
    }

    private fun handleMessage(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title ?: "Sharedule"
        val body = remoteMessage.notification?.body ?: "New notification"
        val message = "$title: $body"

        // Add to NotificationStore for UI
        NotificationStore.notifications.add(message)

        // Show system notification
        NotificationUtils.showNotification(this, title, body)
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(title: String, body: String, clickAction: String?) {
        // Create intent for when notification is tapped
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Add any extra data from click_action if needed
            clickAction?.let { putExtra("click_action", it) }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        // Show the notification
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun sendTokenToServer(token: String) {
        // TODO: Implement sending token to your backend server
        // This is where you would typically make an API call to your server
        // to register the FCM token for the current user
        
        Log.d(TAG, "Token should be sent to server: $token")
        
        // Example implementation:
        // val apiService = RetrofitClient.apiService
        // apiService.registerFcmToken(token).enqueue(object : Callback<ResponseBody> {
        //     override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
        //         Log.d(TAG, "Token registered successfully")
        //     }
        //     override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
        //         Log.e(TAG, "Failed to register token", t)
        //     }
        // })
    }
} 