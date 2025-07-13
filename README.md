# Sharedule - Google Sign-In with Firebase

A modern Android app with Google Sign-In authentication using Firebase and Jetpack Compose.

## Features

- 🔐 Google Sign-In authentication
- 🎨 Modern Material 3 UI with gradient backgrounds
- 📱 Jetpack Compose UI
- 🔄 Real-time authentication state management
- 🚀 Firebase integration

## Setup Instructions

### 1. Firebase Project Setup

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or select an existing one
3. Add an Android app to your Firebase project:
   - Package name: `himanshu.com.sharedule`
   - App nickname: "Sharedule" (optional)
   - Debug signing certificate SHA-1 (optional for now)

### 2. Enable Google Sign-In

1. In Firebase Console, go to **Authentication** > **Sign-in method**
2. Enable **Google** as a sign-in provider
3. Configure the OAuth consent screen if needed
4. Add your support email

### 3. Download Configuration

1. Download the `google-services.json` file from Firebase Console
2. Replace the placeholder `google-services.json` in the `app/` directory with your actual file

### 4. Configure Web Client ID

**Option A: Using Properties File (Recommended)**
1. Copy `app/src/main/assets/config.template.properties` to `app/src/main/assets/config.properties`
2. In Firebase Console, go to **Project Settings** > **General**
3. Scroll down to **Your apps** section
4. Copy the **Web client ID** (ends with `.apps.googleusercontent.com`)
5. Replace `YOUR_WEB_CLIENT_ID` in `config.properties` with your actual Web client ID

**Option B: Using BuildConfig**
1. In Firebase Console, go to **Project Settings** > **General**
2. Scroll down to **Your apps** section
3. Copy the **Web client ID** (ends with `.apps.googleusercontent.com`)
4. Open `app/build.gradle.kts` and replace `"YOUR_WEB_CLIENT_ID"` in the `buildConfigField` with your actual Web client ID

### 5. Build and Run

1. Sync your project with Gradle files
2. Build and run the app on your device or emulator

## Project Structure

```
app/
├── src/main/
│   ├── java/himanshu/com/sharedule/
│   │   ├── auth/
│   │   │   ├── AuthViewModel.kt          # Authentication logic
│   │   │   └── AuthState.kt              # Authentication states
│   │   ├── config/
│   │   │   ├── AppConfig.kt              # Configuration access
│   │   │   └── ConfigManager.kt          # Configuration management
│   │   ├── ui/screens/
│   │   │   ├── LoginScreen.kt            # Google Sign-In screen
│   │   │   └── HomeScreen.kt             # Post-login home screen
│   │   └── MainActivity.kt               # Main activity with navigation
│   ├── assets/
│   │   ├── config.properties             # Configuration file (create from template)
│   │   └── config.template.properties    # Configuration template
│   └── res/
│       └── drawable/
│           └── ic_google.xml             # Google icon
├── google-services.json                  # Firebase configuration
└── build.gradle.kts                     # App-level dependencies
```

## Dependencies

- **Firebase Auth**: For authentication
- **Google Sign-In**: For Google authentication
- **Jetpack Compose**: For modern UI
- **Material 3**: For design components
- **Lifecycle ViewModel**: For state management

## Authentication Flow

1. **Initial State**: App checks if user is already signed in
2. **Login Screen**: Beautiful gradient UI with Google Sign-In button
3. **Authentication**: Handles Google Sign-In flow with Firebase
4. **Home Screen**: Displays user information and sign-out option
5. **State Management**: Real-time updates using StateFlow

## Customization

### Colors
The app uses a beautiful gradient theme. You can customize colors in:
- `LoginScreen.kt` and `HomeScreen.kt` for the gradient
- `ui/theme/Color.kt` for app-wide colors

### UI Components
- Modern card-based design
- Rounded corners and elevation
- Responsive layout
- Loading states and error handling

## Troubleshooting

### Common Issues

1. **"Google Sign-In failed"**
   - Ensure Web client ID is correctly set
   - Check Firebase project configuration
   - Verify Google Sign-In is enabled in Firebase Console

2. **"google-services.json not found"**
   - Download the file from Firebase Console
   - Place it in the `app/` directory
   - Sync project with Gradle files

3. **Build errors**
   - Clean and rebuild project
   - Check all dependencies are properly added
   - Ensure Google Services plugin is applied

### Debug Mode

To enable debug logging, add this to your `MainActivity.kt`:

```kotlin
import com.google.firebase.auth.FirebaseAuth

// In onCreate()
FirebaseAuth.getInstance().setFirebaseAuthSettings(
    FirebaseAuthSettings.Builder().setAppVerificationDisabledForTesting(true).build()
)
```

## Security Notes

- Never commit your actual `google-services.json` to version control
- Never commit your actual `config.properties` to version control
- Use different Firebase projects for development and production
- Implement proper error handling for production apps
- Consider adding additional security measures like app verification
- For production, consider using encrypted storage for sensitive configuration

## Next Steps

After successful authentication, you can extend the app with:
- User profile management
- Schedule/task management features
- Data persistence with Firestore
- Push notifications
- Offline support

## License

This project is for educational purposes. Feel free to use and modify as needed. 