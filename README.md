# SocialAuto - Social Media Automation APK

Automate social media posts on Android. Supports Instagram, TikTok, Twitter/X, Facebook, Threads, Snapchat, and LinkedIn.

## Features

- **Schedule Posts** - Set text, target app, and time
- **Smart Share Mode** - Opens app with pre-filled text (no accessibility needed)
- **Full Automation** - Uses accessibility service + clipboard to paste and post automatically
- **LinkedIn Web Automation** - Built-in WebView for LinkedIn sign-in and posting via JavaScript injection
- **Persistent Storage** - Room database saves scheduled posts
- **AlarmManager Scheduling** - Exact-time execution even when app is closed

## Supported Platforms

| Platform | Smart Share | Full Auto | Notes |
|----------|------------|-----------|-------|
| Instagram | Yes | Yes | May need manual image selection |
| TikTok | Yes | Yes | Works best with text posts |
| Twitter/X | Yes | Yes | Reliable |
| Facebook | Yes | Yes | Reliable |
| Threads | Yes | Yes | Reliable |
| Snapchat | Yes | Partial | Stories need images |
| **LinkedIn** | **WebView** | **Yes** | **Dedicated WebView screen with JS injection** |

## Building the APK

### Requirements
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34

### Steps
1. Open `SocialAuto-Android` in Android Studio
2. Sync Gradle files
3. Build > Generate Signed Bundle/APK (or Build APK for debug)
4. Install on your Samsung S25 or any Android 8+ device

## Setup

### 1. Enable Accessibility Service
- Open app
- Tap the status banner at top
- Enable **SocialAuto Automation** in Accessibility settings
- This allows the app to paste text and click buttons in other apps

### 2. LinkedIn Sign-In
- Tap **"LinkedIn Web Automation"** button on main screen
- Sign into LinkedIn in the WebView (cookies persist across sessions)
- Navigate to Feed via **"Open Feed"** button
- Type your post, tap **"Inject Content"** then **"Submit Post"**

### 3. Schedule a Post
- Tap **+** FAB
- Select target app
- Enter content
- Pick time
- Choose mode:
  - **Smart Share**: Opens app with text ready, you tap final post button
  - **Full Automation**: App opens, pastes text, and tries to click post

## Important Notes

- **Samsung S25**: Fully compatible. Ensure "Put app to sleep" is disabled for SocialAuto in Battery settings.
- **LinkedIn**: Native app automation is supported, but the WebView approach is more reliable due to LinkedIn's UI complexity.
- **Do Not Disturb**: Disable DND or allow SocialAuto notifications for best reliability.
- **Google Play**: This app uses AccessibilityService which may trigger Play Protect warnings. Select "Install anyway."

## File Structure

```
app/src/main/java/com/socialauto/
├── MainActivity.kt           # Main UI & scheduler
├── LinkedInWebActivity.kt    # LinkedIn WebView + JS automation
├── SocialAutoService.kt      # AccessibilityService for full auto
├── Post.kt / PostDao.kt      # Room database entities
├── PostAdapter.kt            # RecyclerView adapter
└── PostAlarmReceiver.kt      # Alarm trigger receiver
```

## Troubleshooting

| Issue | Fix |
|-------|-----|
| Accessibility service turns off | Lock app in recents, disable battery optimization |
| Post not clicking | App UI may have updated. Use Smart Share mode instead |
| LinkedIn not loading | Check internet, ensure mobile site loads in Chrome first |
| Scheduled post didn't fire | Allow exact alarms in Settings > Apps > SocialAuto > Alarms |

## Disclaimer

Use this tool only for accounts you own or have permission to manage. Automation may violate some platforms' Terms of Service. Use at your own risk.
