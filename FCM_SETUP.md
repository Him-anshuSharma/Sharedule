# Firebase Cloud Messaging (FCM) Setup Guide

This guide explains how to set up and use Firebase Cloud Messaging in the Sharedule Android app.

## Overview

The FCM implementation includes:
- `FCMService`: Handles incoming messages and notifications
- `FCMTokenManager`: Utility class for token management
- `FCMViewModel`: ViewModel for UI interactions
- `FCMTestScreen`: Test screen for FCM functionality

## Setup Instructions

### 1. Firebase Console Setup

1. Go to the [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or select an existing one
3. Add your Android app to the project:
   - Package name: `himanshu.com.sharedule`
   - Download the `google-services.json` file
   - Place it in the `app/` directory

### 2. Dependencies

The following dependencies are already added to your project:

```kotlin
// In app/build.gradle.kts
implementation(libs.firebase.messaging)

// In gradle/libs.versions.toml
firebase-messaging = { group = "com.google.firebase", name = "firebase-messaging" }
```

### 3. AndroidManifest.xml

The manifest has been updated with:
- Required permissions for FCM
- FCM service declaration
- Default notification settings

### 4. FCM Service Components

#### FCMService.kt
- Extends `FirebaseMessagingService`
- Handles incoming messages (`onMessageReceived`)
- Manages token refresh (`onNewToken`)
- Creates and displays notifications
- Supports both foreground and background messages

#### FCMTokenManager.kt
- Utility class for token operations
- Methods for getting, deleting tokens
- Topic subscription/unsubscription
- Auto-init management

#### FCMViewModel.kt
- ViewModel for UI state management
- Reactive state flows for token, loading, errors
- Methods for all FCM operations

## Usage

### Basic Token Management

```kotlin
// Get current token
val token = FCMTokenManager.getToken()

// Delete token
FCMTokenManager.deleteToken()

// Subscribe to topic
FCMTokenManager.subscribeToTopic("news")

// Unsubscribe from topic
FCMTokenManager.unsubscribeFromTopic("news")
```

### Using FCMViewModel in UI

```kotlin
@Composable
fun MyScreen(fcmViewModel: FCMViewModel = viewModel()) {
    val fcmToken by fcmViewModel.fcmToken.collectAsState()
    val isLoading by fcmViewModel.isLoading.collectAsState()
    
    // Load token
    fcmViewModel.loadFCMToken()
    
    // Subscribe to topic
    fcmViewModel.subscribeToTopic("user_123")
}
```

### Testing FCM

Use the `FCMTestScreen` to:
- View current FCM token
- Refresh or delete token
- Subscribe/unsubscribe to topics
- Toggle auto-init
- Test notification handling

## Sending Test Messages

### From Firebase Console

1. Go to Firebase Console > Messaging
2. Click "Send your first message"
3. Fill in notification details
4. Target your app
5. Send the message

### Using FCM API

```bash
curl -X POST -H "Authorization: key=YOUR_SERVER_KEY" \
     -H "Content-Type: application/json" \
     -d '{
       "to": "DEVICE_TOKEN",
       "notification": {
         "title": "Test Title",
         "body": "Test Body"
       },
       "data": {
         "click_action": "OPEN_ACTIVITY",
         "custom_key": "custom_value"
       }
     }' \
     https://fcm.googleapis.com/fcm/send
```

## Customization

### Notification Appearance

Modify `FCMService.kt` to customize:
- Notification icon
- Sound and vibration
- Notification channel settings
- Click actions

### Message Handling

Customize `onMessageReceived()` to:
- Handle different message types
- Process custom data payloads
- Implement specific business logic

### Token Management

Extend `sendTokenToServer()` in `FCMService.kt` to:
- Send token to your backend
- Associate token with user account
- Handle token refresh on server

## Troubleshooting

### Common Issues

1. **Token not generated**: Check internet connection and Firebase setup
2. **Notifications not showing**: Verify notification permissions
3. **Service not starting**: Check AndroidManifest.xml configuration

### Debug Logs

Enable debug logging by checking logcat with tag:
- `FCMService`
- `FCMTokenManager`
- `FCMViewModel`

### Testing Checklist

- [ ] App installed and running
- [ ] Internet connection available
- [ ] Firebase project configured
- [ ] `google-services.json` in place
- [ ] FCM token generated
- [ ] Notification permissions granted
- [ ] Test message sent successfully

## Security Considerations

1. **Server Key**: Keep your FCM server key secure
2. **Token Storage**: Store tokens securely on your server
3. **Message Validation**: Validate incoming messages
4. **User Consent**: Ensure users consent to notifications

## Next Steps

1. Implement server-side token storage
2. Add user-specific topic subscriptions
3. Create custom notification actions
4. Implement message analytics
5. Add notification preferences UI

## Support

For issues or questions:
1. Check Firebase documentation
2. Review Android FCM guide
3. Check logcat for error messages
4. Verify all setup steps completed 