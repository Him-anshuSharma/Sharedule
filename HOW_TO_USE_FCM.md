# How to Use FCM in Sharedule App

## Quick Start Guide

### 1. Setup Firebase (One-time setup)

1. **Go to Firebase Console**: https://console.firebase.google.com/
2. **Create/Select Project**: Create a new project or select existing one
3. **Add Android App**:
   - Package name: `himanshu.com.sharedule`
   - Download `google-services.json`
   - Place it in the `app/` folder of your project

### 2. Test FCM in the App

The app now has FCM testing buttons on the home screen:

#### **"Test Received" Button** (Green)
- Subscribes you to topic: `test_notifications`
- Use this to test receiving notifications
- After clicking, send a test message from Firebase Console to topic `test_notifications`

#### **"Test Send" Button** (Blue)
- Subscribes you to topic: `send_test`
- Use this to test sending notifications
- After clicking, send a test message from Firebase Console to topic `send_test`

#### **"Refresh FCM Token" Button** (Orange)
- Refreshes your FCM token
- Shows the current token (first 20 characters)
- Use this if you need a new token

### 3. Send Test Notifications

#### From Firebase Console (Easiest):
1. Go to Firebase Console → Messaging
2. Click "Send your first message"
3. Fill in:
   - **Notification title**: "Test Message"
   - **Notification text**: "Hello from Sharedule!"
4. Under "Target", select "Topic" and choose:
   - `test_notifications` (for "Test Received" button)
   - `send_test` (for "Test Send" button)
5. Click "Send"

#### From Command Line:
```bash
curl -X POST -H "Authorization: key=YOUR_SERVER_KEY" \
     -H "Content-Type: application/json" \
     -d '{
       "to": "/topics/test_notifications",
       "notification": {
         "title": "Test Message",
         "body": "Hello from Sharedule!"
       }
     }' \
     https://fcm.googleapis.com/fcm/send
```

### 4. What Happens When You Receive a Notification

1. **App in Foreground**: Notification appears in the app
2. **App in Background**: System notification appears
3. **App Closed**: System notification appears
4. **Tap Notification**: Opens the app

### 5. Troubleshooting

#### Notifications Not Appearing?
- Check internet connection
- Verify `google-services.json` is in `app/` folder
- Check notification permissions in device settings
- Look at logcat for error messages

#### Token Not Showing?
- Click "Refresh FCM Token" button
- Check internet connection
- Look for errors in the red error box

#### Buttons Not Working?
- Check if you're signed in
- Look for error messages in the red box
- Try refreshing the token first

### 6. Advanced Usage

#### Subscribe to Custom Topics:
```kotlin
// In your code
fcmViewModel.subscribeToTopic("my_custom_topic")
```

#### Send to Specific User:
```bash
curl -X POST -H "Authorization: key=YOUR_SERVER_KEY" \
     -H "Content-Type: application/json" \
     -d '{
       "to": "DEVICE_TOKEN_HERE",
       "notification": {
         "title": "Personal Message",
         "body": "This is for you!"
       }
     }' \
     https://fcm.googleapis.com/fcm/send
```

#### Send with Custom Data:
```bash
curl -X POST -H "Authorization: key=YOUR_SERVER_KEY" \
     -H "Content-Type: application/json" \
     -d '{
       "to": "/topics/test_notifications",
       "notification": {
         "title": "Data Message",
         "body": "Check the data!"
       },
       "data": {
         "action": "open_schedule",
         "schedule_id": "123"
       }
     }' \
     https://fcm.googleapis.com/fcm/send
```

### 7. Next Steps

1. **Test the buttons** on the home screen
2. **Send test messages** from Firebase Console
3. **Customize notifications** in `FCMService.kt`
4. **Add server integration** to store tokens
5. **Implement user-specific topics**

### 8. Get Your Server Key

1. Go to Firebase Console → Project Settings
2. Click "Cloud Messaging" tab
3. Copy the "Server key" (starts with `AAAA...`)
4. Use this key in the curl commands above

---

**That's it!** Your app now supports push notifications. Test it out and let me know if you need help with anything specific. 