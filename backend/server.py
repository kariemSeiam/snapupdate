#!/usr/bin/env python3
"""
SnapUpdate Backend Server
Main entry point for the Flask application
"""

import os
import sys
from app import create_app

def main():
    """Main server entry point"""
    print("🎨 SnapUpdate Backend Server")
    print("=" * 50)
    
    # Create Flask app
    app = create_app()
    
    # Configuration - Expert setup with network access
    host = os.getenv('HOST', '0.0.0.0')  # Changed from 'localhost' to '0.0.0.0'
    port = int(os.getenv('PORT', 5000))
    debug = os.getenv('DEBUG', 'True').lower() == 'true'
    
    print(f"🚀 Starting server on {host}:{port}")
    print(f"🔧 Debug mode: {debug}")
    print(f"📱 Android app will connect to: http://192.168.1.202:{port}")
    print(f"🌐 Web interface: http://localhost:{port}")
    print(f"🌐 Network access: http://192.168.1.202:{port}")
    print(f"🔗 API Base URL: http://192.168.1.202:{port}/api/v1")
    print("\n📋 Available endpoints:")
    print("   - GET /api/v1/update - Check for updates")
    print("   - GET /api/v1/health - Health check")
    print("   - GET /api/v1/versions - Get all versions")
    print("   - GET /api/v1/download/<version> - Download APK")
    print("   - GET /api/v1/stats - Server statistics")
    print("   - GET /api/v1/apks/available - Get all available APK files")
    print("   - GET /api/v1/version/current - Get current server version")
    print("   - POST /api/v1/version/increment - Increment version")
    print("   - POST /api/v1/version/reset - Reset to v1.0 (complete cycle)")
    print("\n💡 Press Ctrl+C to stop the server")
    print("=" * 50)
    
    # Run the server
    app.run(
        host=host,
        port=port,
        debug=debug
    )

if __name__ == '__main__':
    app = create_app()
    app.run(host='0.0.0.0', port=5000, debug=True)  # Changed from 'localhost' to '0.0.0.0' 