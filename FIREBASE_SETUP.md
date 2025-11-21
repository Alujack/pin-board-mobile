# Firebase Setup for PinBoard Mobile App

## Prerequisites
- Android Studio installed
- A Google account
- Access to Firebase Console

## Step 1: Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project" or select an existing project
3. Enter project name: `PinBoard` (or your preferred name)
4. Follow the setup wizard

## Step 2: Add Android App to Firebase

1. In Firebase Console, click on the Android icon to add an Android app
2. Register your app with these details:
   - **Android package name**: `kh.edu.rupp.fe.ite.pinboard`
   - **App nickname** (optional): `PinBoard Mobile`
   - **Debug signing certificate SHA-1** (optional for now)

3. Download the `google-services.json` file

## Step 3: Add google-services.json to Your Project

1. Place the downloaded `google-services.json` file in:
   ```
   pin-board-mobile/app/google-services.json
   ```

2. The file should be at the same level as your `build.gradle.kts` file

## Step 4: Update build.gradle Files

### Project-level build.gradle.kts (Already configured)
The project should already have the Google Services plugin. Verify this in `build.gradle.kts`:

```kotlin
plugins {
    id("com.google.gms.google-services") version "4.4.0" apply false
}
```

### App-level build.gradle.kts (Already configured)
The app module should already have:

```kotlin
plugins {
    // ... other plugins
    id("com.google.gms.google-services")
}

dependencies {
    // Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))
    implementation("com.google.firebase:firebase-messaging-ktx:24.0.0")
}
```

## Step 5: Enable Firebase Cloud Messaging

1. In Firebase Console, go to **Build** > **Cloud Messaging**
2. Enable **Cloud Messaging API** if not already enabled
3. Note your **Server Key** (you'll need this for the backend)

## Step 6: Configure Backend with FCM Server Key

1. In your backend `.env` file, add:
   ```
   FCM_SERVER_KEY=your_firebase_server_key_here
   ```

2. The backend should use this key to send push notifications

## Step 7: Test Push Notifications

### Method 1: Using Firebase Console
1. Go to **Build** > **Cloud Messaging**
2. Click **Send your first message**
3. Enter notification title and text
4. Click **Send test message**
5. Enter your FCM token (check app logs for token)

### Method 2: Using Backend API
Once logged in, the app will automatically register its FCM token with the backend. The backend will send notifications for:
- Pin likes
- Comments
- New followers
- Pin saves

## Troubleshooting

### Token not registering
- Check if `google-services.json` is in the correct location
- Verify the package name matches in Firebase Console
- Check app logs for FCM token

### Notifications not appearing
- Ensure POST_NOTIFICATIONS permission is granted (Android 13+)
- Check notification channel is created
- Verify FCM server key is correct in backend

### Build errors
- Clean and rebuild project: `Build > Clean Project` then `Build > Rebuild Project`
- Sync Gradle files: `File > Sync Project with Gradle Files`
- Check that google-services plugin is applied

## Features Implemented

### Push Notifications
- ✅ FCM token registration
- ✅ Automatic token refresh
- ✅ Notification handling
- ✅ Deep linking support

### Notification Types
- ✅ Pin likes
- ✅ Comments
- ✅ Replies
- ✅ New followers
- ✅ Pin saves

### UI Features
- ✅ In-app notification center
- ✅ Unread notification badges
- ✅ Mark as read functionality
- ✅ Pull-to-refresh

## API Endpoints Used

### Notification APIs
- `POST /notifications/register-token` - Register FCM token
- `GET /notifications` - Get user notifications
- `POST /notifications/mark-read` - Mark notification as read
- `POST /notifications/mark-all-read` - Mark all as read

### Interaction APIs
- `POST /pinLike/togglePinLike` - Like/unlike pin
- `POST /share/sharePin` - Share pin
- `POST /api/comment/createComment` - Create comment
- `POST /api/comment/toggleCommentLike` - Like/unlike comment

## Security Notes

1. **Never commit google-services.json to public repositories**
   - Add to `.gitignore` if needed
   - Use different Firebase projects for dev/prod

2. **Secure your FCM Server Key**
   - Store in environment variables
   - Never expose in client code

3. **Validate notification payloads**
   - Backend should validate all notification data
   - Implement rate limiting

## Additional Resources

- [Firebase Cloud Messaging Documentation](https://firebase.google.com/docs/cloud-messaging)
- [Android Push Notifications Guide](https://firebase.google.com/docs/cloud-messaging/android/client)
- [Firebase Console](https://console.firebase.google.com/)

