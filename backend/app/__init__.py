"""
SnapUpdate Backend Application
Flask server for handling app updates
"""

from flask import Flask
from flask_cors import CORS

def create_app():
    """Application factory pattern"""
    app = Flask(__name__)
    CORS(app)
    
    # Import and register blueprints
    from .routes import api_bp
    app.register_blueprint(api_bp, url_prefix='/api/v1')
    
    return app 