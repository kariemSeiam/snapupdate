# SnapUpdate Backend Server

A Flask-based backend server for managing Android app updates with a complete version cycle system.

## 🎯 Overview

SnapUpdate Backend provides a RESTful API for Android app update management, featuring:
- **Version Cycle Management**: Complete lifecycle from v1.0 to v1.3 with reset capability
- **APK File Management**: Automated APK file creation and distribution
- **Update Detection**: Real-time version checking and update notifications
- **Statistics Tracking**: Comprehensive download and version statistics
- **Force Update Support**: Mandatory update enforcement for critical releases

## 🏗️ Architecture

```
backend/
├── app/
│   ├── __init__.py          # Flask app factory
│   └── routes.py            # API endpoints
├── data/
│   ├── apks/                # APK file storage
│   │   ├── SnapUpdate-v1.0.apk
│   │   ├── SnapUpdate-v1.1.apk
│   │   ├── SnapUpdate-v1.2.apk
│   │   └── SnapUpdate-v1.3.apk
│   ├── versions/
│   │   └── versions.json    # Version metadata
│   ├── stats.json           # Server statistics
│   └── version_manager.py   # Version management logic
├── logs/                    # Application logs
├── server.py               # Main entry point
├── requirements.txt        # Python dependencies
└── setup.py               # Installation script
```

## 🔄 Version Cycle System

The backend implements a complete version cycle with the following flow:

### Initial Cycle (v1.0 → v1.3)
1. **v1.0**: Initial release with basic update functionality
2. **v1.1**: Enhanced UI/UX with Material 3 design
3. **v1.2**: Auto-installation feature and performance improvements
4. **v1.3**: Latest version with advanced features

### Reset Cycle
- **Complete Reset**: Reset to v1.0 to start a new development cycle
- **APK Preservation**: All APK files remain available for download
- **Version Management**: Only version metadata is reset
- **Statistics**: Reset tracking maintains historical data

## 🚀 Quick Start

### Prerequisites
- Python 3.8+
- Flask 2.3.3+
- Flask-CORS 4.0.0+

### Installation
```bash
# Clone the repository
git clone <repository-url>
cd SnapUpdate/backend

# Install dependencies
pip install -r requirements.txt

# Run the server
python server.py
```

### Environment Variables
```bash
HOST=0.0.0.0          # Server host (default: 0.0.0.0)
PORT=5000              # Server port (default: 5000)
DEBUG=True             # Debug mode (default: True)
```

## 📡 API Endpoints

### Core Update Endpoints

#### `GET /api/v1/update`
Check for app updates with current version comparison.

**Parameters:**
- `version` (query): Current app version (default: "1.0")

**Response:**
```json
{
  "versionCode": 3,
  "versionName": "1.2",
  "downloadUrl": "http://server:5000/api/v1/download/1.2",
  "releaseNotes": "Added auto-installation feature",
  "isForceUpdate": true
}
```

#### `GET /api/v1/download/<version>`
Download APK file for specific version.

**Parameters:**
- `version` (path): Version to download (e.g., "1.2")

**Response:** APK file download

### Version Management

#### `GET /api/v1/versions`
Get all available versions with metadata.

**Response:**
```json
{
  "versions": [
    {
      "versionCode": 1,
      "versionName": "1.0",
      "releaseNotes": "Initial release",
      "downloadUrl": "http://server:5000/api/v1/download/1.0",
      "isForceUpdate": false,
      "createdAt": "2024-01-01T00:00:00Z"
    }
  ],
  "total": 4
}
```

#### `POST /api/v1/version/increment`
Create new version and APK file.

**Request Body:**
```json
{
  "version": "1.4",
  "releaseNotes": "New features added",
  "isForceUpdate": false
}
```

#### `POST /api/v1/version/reset`
Reset to specific version (complete cycle reset).

**Request Body:**
```json
{
  "targetVersion": "1.0",
  "reason": "Reset to start new version cycle"
}
```

### Server Information

#### `GET /api/v1/health`
Health check endpoint.

**Response:**
```json
{
  "status": "healthy",
  "server_version": "1.0.0",
  "uptime": "running"
}
```

#### `GET /api/v1/stats`
Get server statistics.

**Response:**
```json
{
  "total_versions": 4,
  "versions_created": 2,
  "versions_updated": 1,
  "versions_deleted": 0,
  "versions_reset": 1,
  "downloads": 15,
  "last_updated": "2024-01-30T12:00:00Z"
}
```

#### `GET /api/v1/apks/available`
Get all available APK files on server.

**Response:**
```json
{
  "available_apks": ["1.0", "1.1", "1.2", "1.3"],
  "total_apks": 4
}
```

#### `GET /api/v1/version/current`
Get current server version.

**Response:**
```json
{
  "currentVersion": "1.3",
  "versionCode": 4,
  "releaseNotes": "Latest features",
  "isForceUpdate": false
}
```

## 🔧 Version Manager

The `VersionManager` class handles all version-related operations:

### Key Features
- **Demo Data Initialization**: Pre-populated with v1.0-v1.3 versions
- **APK File Management**: Automatic APK file creation and storage
- **Version Metadata**: JSON-based version information storage
- **Statistics Tracking**: Download and version operation counters
- **Reset Functionality**: Complete cycle reset with APK preservation

### Version Lifecycle
1. **Creation**: New versions added via API or initialization
2. **Distribution**: APK files served for download
3. **Tracking**: Statistics updated for each operation
4. **Reset**: Complete cycle reset to start new development phase

## 📊 Statistics System

The backend tracks comprehensive statistics:
- **Version Operations**: Created, updated, deleted, reset
- **Download Counts**: Total APK downloads
- **Timestamps**: Last update tracking
- **Version Totals**: Current version count

## 🔒 Security Features

- **CORS Support**: Cross-origin request handling
- **Error Handling**: Comprehensive exception management
- **Input Validation**: Request parameter validation
- **File Security**: Safe APK file serving

## 🧪 Testing

### Demo Data
The server includes pre-configured demo data:
- **Versions**: v1.0, v1.1, v1.2, v1.3
- **APK Files**: Demo APK files for each version
- **Statistics**: Initial statistics tracking

### Android App Integration
- **Emulator**: Connect via `http://10.0.2.2:5000`
- **Physical Device**: Connect via server IP address
- **Web Interface**: Access via `http://localhost:5000`

## 🚀 Deployment

### Development
```bash
python server.py
```

### Production
```bash
# Set environment variables
export HOST=0.0.0.0
export PORT=5000
export DEBUG=False

# Run with production server
gunicorn -w 4 -b 0.0.0.0:5000 server:app
```

## 📝 Logging

Logs are stored in the `logs/` directory:
- **Application Logs**: Server operation logs
- **Error Logs**: Exception and error tracking
- **Access Logs**: API request logging

## 🔄 Complete Cycle Example

### Starting New Cycle
1. **Current State**: v1.3 (latest)
2. **Reset Command**: `POST /api/v1/version/reset`
3. **Reset Data**: `{"targetVersion": "1.0", "reason": "New cycle"}`
4. **Result**: Version management reset to v1.0
5. **APK Files**: All previous APKs remain available
6. **New Cycle**: Ready for v1.1, v1.2, v1.3 development

### Version Progression
```bash
# Check current version
GET /api/v1/version/current

# Create new version
POST /api/v1/version/increment
{
  "version": "1.1",
  "releaseNotes": "Enhanced UI/UX",
  "isForceUpdate": false
}

# Check for updates
GET /api/v1/update?version=1.0

# Download APK
GET /api/v1/download/1.1
```

## 🤝 Contributing

1. Fork the repository
2. Create feature branch
3. Implement changes
4. Test thoroughly
5. Submit pull request

## 📄 License

This project is licensed under the MIT License.

---

**SnapUpdate Backend** - Complete version cycle management for Android app updates. 