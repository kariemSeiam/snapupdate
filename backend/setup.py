#!/usr/bin/env python3
"""
SnapUpdate Backend Setup Script
"""

import subprocess
import sys
import os

def install_requirements():
    """Install Flask dependencies"""
    print("📦 Installing backend dependencies...")
    try:
        subprocess.check_call([sys.executable, "-m", "pip", "install", "-r", "requirements.txt"])
        print("✅ Dependencies installed successfully!")
        return True
    except subprocess.CalledProcessError as e:
        print(f"❌ Failed to install dependencies: {e}")
        return False

def create_directories():
    """Create necessary directories"""
    print("📁 Creating directories...")
    directories = [
        "data/versions",
        "data/apks",
        "logs"
    ]
    
    for directory in directories:
        os.makedirs(directory, exist_ok=True)
        print(f"✅ Created: {directory}")

def main():
    print("🎨 SnapUpdate Backend Setup")
    print("=" * 40)
    
    # Check if requirements.txt exists
    if not os.path.exists("requirements.txt"):
        print("❌ requirements.txt not found!")
        return
    
    # Install dependencies
    if not install_requirements():
        return
    
    # Create directories
    create_directories()
    
    print("\n🎯 Backend setup complete!")
    print("🚀 To start the server, run: python server.py")
    print("📱 Android app will connect to: https://geolink.pythonanywhere.com")
    print("🌐 Web interface: https://geolink.pythonanywhere.com")
    print("=" * 40)

if __name__ == "__main__":
    main() 