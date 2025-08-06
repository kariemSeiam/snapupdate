# SnapUpdate - Android Update System

Complete Android update management with automatic installation and version cycle control.

## Features

- **Version Management**: 1.0 → 1.1 → 1.2 → 1.3 (3 increments max)
- **Reset System**: Restart cycle anytime with persistent APK storage
- **Auto-Installation**: Bulletproof download → install flow
- **Native Detection**: Reads app version from PackageManager
- **Clean UI**: Kotlin colors, responsive layout
- **GitHub Integration**: Clickable repo chip with animations
- **Progress Simulation**: Realistic download progress with speed/time
- **Triple Monitoring**: BroadcastReceiver + Polling + Direct monitoring
- **APK Persistence**: Files survive resets for app responsiveness

---

## Screenshots

### Home Screen
![Home Screen](images/home-screen.png)
*Main interface with version cards and action buttons*

### Update Dialog
![Update Dialog](images/update-dialog.png)
*Update confirmation with version information and release notes*

### Install Dialog
![Install Dialog](images/install-dialog.png)
*Download progress with realistic speed and time simulation*

---

## Quick Start

### Backend
```bash
cd backend
pip install -r requirements.txt
python server.py
```

### Android
```bash
# Open in Android Studio
# Build and run
```

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/update` | GET | Check for updates |
| `/api/v1/version/increment` | POST | Increment version |
| `/api/v1/version/reset` | POST | Reset to v1.0 |
| `/api/v1/download/<version>` | GET | Download APK |
| `/api/v1/apks/available` | GET | Get all available APK files |

## Architecture

```
📱 Android App (Jetpack Compose)
├── UI Components
├── Update Management
├── Download Manager
└── Installation System

🌐 Flask Backend (Python)
├── Version Manager
├── APK Storage
└── Reset System
```

## Key Files

- **HomeScreen.kt**: Main UI with version cards
- **UpdateDialog.kt**: Update confirmation dialog
- **InstallDialog.kt**: Download progress dialog
- **UpdateViewModel.kt**: State management
- **routes.py**: Backend API endpoints
- **version_manager.py**: Version management logic

## Version Cycle

1. **Start**: v1.0
2. **Increment**: v1.1 → v1.2 → v1.3
3. **Reset**: Back to v1.0 anytime
4. **APK Persistence**: Files survive resets

## Tech Stack

- **Frontend**: Kotlin, Jetpack Compose, Coroutines
- **Backend**: Python, Flask, JSON
- **Permissions**: Install packages, storage access

---

**Built with Kotlin & Python** 