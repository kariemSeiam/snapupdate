#!/usr/bin/env python3
"""
WSGI entry point for SnapUpdate Backend
PythonAnywhere deployment configuration
"""

import sys
import os

# Ensure the directory containing your Flask app is in the Python path
path = '/home/geolink/backend'
if path not in sys.path:
    sys.path.append(path)

from app import create_app

application = create_app()

if __name__ == '__main__':
    application.run() 