# SnapUpdate 🚀

Complete Android app update system with Flask backend and Material 3 UI. Features version cycle management, auto-installation, and real-time update detection.

[![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Flask](https://img.shields.io/badge/Flask-000000?style=for-the-badge&logo=flask&logoColor=white)](https://flask.palletsprojects.com/)
[![Python](https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=python&logoColor=white)](https://www.python.org/)

## 🎯 Overview

SnapUpdate is a comprehensive Android app update management system that combines a modern Material 3 Android application with a powerful Flask backend API. The system provides seamless version cycle management, automatic update detection, and APK distribution capabilities.

### ✨ Key Features

- **🔄 Version Cycle Management**: Complete lifecycle from v1.0 to v1.3 with reset capability
- **📱 Material 3 UI**: Modern Android interface with Jetpack Compose
- **🚀 Auto-Installation**: Seamless APK installation and update process
- **📊 Real-time Updates**: Live update detection and notification system
- **🔧 Force Update Support**: Mandatory update enforcement for critical releases
- **📈 Statistics Tracking**: Comprehensive download and version analytics
- **🌐 RESTful API**: Complete backend API for update management

## 🏗️ Architecture

```
SnapUpdate/
├── app/                    # Android Application
│   ├── src/main/
│   │   ├── java/com/pigo/snapupdate/
│   │   │   ├── data/          # API service & network layer
│   │   │   ├── ui/            # UI components & screens
│   │   │   ├── utils/         # Utilities & managers
│   │   │   └── MainActivity.kt
│   │   └── res/               # Android resources
│   └── build.gradle.kts
├── backend/                 # Flask Backend Server
│   ├── app/                 # Flask application
│   ├── data/                # Version management & APK storage
│   ├── server.py            # Main server entry point
│   └── requirements.txt     # Python dependencies
└── README.md               # This file
```

## 🚀 Quick Start

### Prerequisites

- **Android Development**: Android Studio, JDK 17+
- **Backend**: Python 3.8+, Flask 2.3.3+
- **Network**: Local network access for Android emulator

### 1. Backend Setup

```bash
# Navigate to backend directory
cd backend

# Install Python dependencies
pip install -r requirements.txt

# Start the Flask server
python server.py
```

**Server will start on:** `http://localhost:5000`
**Android emulator endpoint:** `http://10.0.2.2:5000`

### 2. Android App Setup

```bash
# Open in Android Studio
# Navigate to app/ directory
# Sync Gradle files
# Run on emulator or device
```

## 📱 Android App Features

### 🎨 Material 3 Design
- **Dynamic Color**: Adaptive theming based on system colors
- **Responsive Layout**: Optimized for all screen sizes
- **Accessibility**: Full WCAG 3 compliance
- **Dark Mode**: Automatic theme switching

### 🔄 Update Management
- **Version Detection**: Real-time update checking
- **Auto-Download**: Background APK downloading
- **Installation**: Seamless APK installation
- **Progress Tracking**: Download and install progress

### 🛠️ Technical Stack
- **Jetpack Compose**: Modern UI toolkit
- **Material 3**: Latest Material Design
- **Retrofit**: Network API communication
- **Coroutines**: Asynchronous operations
- **ViewModel**: Architecture components

## 🌐 Backend API

### Core Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/update` | GET | Check for app updates |
| `/api/v1/download/<version>` | GET | Download APK file |
| `/api/v1/versions` | GET | Get all versions |
| `/api/v1/stats` | GET | Server statistics |
| `/api/v1/version/increment` | POST | Create new version |
| `/api/v1/version/reset` | POST | Reset version cycle |

### Example Usage

```bash
# Check for updates
curl "http://localhost:5000/api/v1/update?version=1.0"

# Download APK
curl "http://localhost:5000/api/v1/download/1.2" -o SnapUpdate-v1.2.apk

# Get server stats
curl "http://localhost:5000/api/v1/stats"
```

## 🔄 Version Cycle System

### Initial Cycle (v1.0 → v1.3)
1. **v1.0**: Initial release with basic update functionality
2. **v1.1**: Enhanced UI/UX with Material 3 design
3. **v1.2**: Auto-installation feature and performance improvements
4. **v1.3**: Latest version with advanced features

### Reset Cycle
- **Complete Reset**: Reset to v1.0 to start new development cycle
- **APK Preservation**: All APK files remain available for download
- **Version Management**: Only version metadata is reset
- **Statistics**: Reset tracking maintains historical data

## 📊 Demo Data

The system includes pre-configured demo data:

### Versions
- **v1.0**: Initial release with basic update functionality
- **v1.1**: Enhanced UI/UX with Material 3 design
- **v1.2**: Auto-installation feature and performance improvements
- **v1.3**: Latest version with advanced features

### APK Files
- Demo APK files for each version
- Automatic APK file creation
- Download tracking and statistics

## 🛠️ Development

### Android Development
```bash
# Open in Android Studio
# Build and run on emulator
# Test with backend server running
```

### Backend Development
```bash
cd backend
python server.py
# Server runs on http://localhost:5000
```

### API Testing
```bash
# Test update endpoint
curl "http://localhost:5000/api/v1/update?version=1.0"

# Test version creation
curl -X POST "http://localhost:5000/api/v1/version/increment" \
  -H "Content-Type: application/json" \
  -d '{"version": "1.4", "releaseNotes": "New features"}'
```

## 🔧 Configuration

### Environment Variables
```bash
# Backend
HOST=0.0.0.0          # Server host
PORT=5000              # Server port
DEBUG=True             # Debug mode

# Android
# Configure in app/src/main/java/com/pigo/snapupdate/data/ApiService.kt
```

### Network Configuration
- **Emulator**: `http://10.0.2.2:5000`
- **Physical Device**: Use server IP address
- **Web Interface**: `http://localhost:5000`

## 📈 Statistics & Monitoring

The system tracks comprehensive statistics:
- **Version Operations**: Created, updated, deleted, reset
- **Download Counts**: Total APK downloads
- **Update Checks**: Version comparison requests
- **Performance Metrics**: Response times and throughput

## 🔒 Security Features

- **CORS Support**: Cross-origin request handling
- **Error Handling**: Comprehensive exception management
- **Input Validation**: Request parameter validation
- **File Security**: Safe APK file serving

## 🧪 Testing

### Android App Testing
- **Emulator Testing**: Test with Android emulator
- **Physical Device**: Test on real Android device
- **Network Testing**: Test with backend server

### Backend API Testing
- **Health Check**: `GET /api/v1/health`
- **Update Detection**: `GET /api/v1/update`
- **Version Management**: Test all CRUD operations

## 🚀 Deployment

### Backend Deployment
```bash
# Production setup
export HOST=0.0.0.0
export PORT=5000
export DEBUG=False

# Run with Gunicorn
pip install gunicorn
gunicorn -w 4 -b 0.0.0.0:5000 server:app
```

### Android App Deployment
- **Build APK**: Generate signed APK
- **Upload to Backend**: Place APK in `backend/data/apks/`
- **Update Version**: Add version metadata
- **Test Distribution**: Verify download and installation

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Material 3**: Google's latest Material Design system
- **Jetpack Compose**: Modern Android UI toolkit
- **Flask**: Lightweight Python web framework
- **Android Studio**: Official Android development IDE

---

**SnapUpdate** - Complete version cycle management for Android app updates. 🚀

*Built with ❤️ using Kotlin, Jetpack Compose, Material 3, Flask, and Python.* 