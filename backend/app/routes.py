"""
API Routes for SnapUpdate Backend
"""

from flask import Blueprint, jsonify, request, send_file
import os
import sys
sys.path.append(os.path.join(os.path.dirname(__file__), '..'))
from data.version_manager import VersionManager

# Create blueprint
api_bp = Blueprint('api', __name__)

# Initialize version manager
version_manager = VersionManager()

@api_bp.route('/update', methods=['GET'])
def check_update():
    """Endpoint to check for app updates"""
    try:
        current_version = request.args.get('version', '1.0')
        latest_version = version_manager.get_latest_version()
        
        if latest_version and latest_version['versionName'] != current_version:
            # Get the base URL from the request
            base_url = request.host_url.rstrip('/')
            download_url = f"{base_url}/api/v1/download/{latest_version['versionName']}"
            
            return jsonify({
                'versionCode': latest_version['versionCode'],
                'versionName': latest_version['versionName'],
                'downloadUrl': download_url,
                'releaseNotes': latest_version['releaseNotes'],
                'isForceUpdate': latest_version.get('isForceUpdate', False)
            })
        else:
            # Return 200 with no update message instead of 404
            return jsonify({
                'message': 'No update available',
                'currentVersion': current_version,
                'latestVersion': latest_version['versionName'] if latest_version else current_version
            }), 200
            
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@api_bp.route('/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    return jsonify({
        'status': 'healthy',
        'server_version': '1.0.0',
        'uptime': 'running'
    })

@api_bp.route('/versions', methods=['GET'])
def get_all_versions():
    """Get all available versions"""
    try:
        versions = version_manager.get_all_versions()
        return jsonify({
            'versions': versions,
            'total': len(versions)
        })
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@api_bp.route('/download/<version>', methods=['GET'])
def download_apk(version):
    """Download APK file for specific version"""
    try:
        apk_path = version_manager.get_apk_path(version)
        if os.path.exists(apk_path):
            return send_file(apk_path, as_attachment=True)
        else:
            return jsonify({'error': 'APK not found'}), 404
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@api_bp.route('/stats', methods=['GET'])
def get_stats():
    """Get server statistics"""
    try:
        stats = version_manager.get_stats()
        return jsonify(stats)
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@api_bp.route('/apks/available', methods=['GET'])
def get_available_apks():
    """Get all available APK files on server"""
    try:
        available_apks = version_manager.get_all_available_apks()
        return jsonify({
            'available_apks': available_apks,
            'total_apks': len(available_apks)
        })
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@api_bp.route('/version/increment', methods=['POST'])
def increment_version():
    """Increment server version and create new APK"""
    try:
        data = request.get_json()
        new_version = data.get('version')
        release_notes = data.get('releaseNotes', '')
        is_force_update = data.get('isForceUpdate', False)
        
        # Get the base URL from the request
        base_url = request.host_url.rstrip('/')
        download_url = f"{base_url}/api/v1/download/{new_version}"
        
        if version_manager.add_version({
            'versionName': new_version,
            'versionCode': version_manager.get_next_version_code(),
            'releaseNotes': release_notes,
            'isForceUpdate': is_force_update,
            'downloadUrl': download_url
        }):
            return jsonify({
                'success': True,
                'message': f'Version {new_version} created successfully',
                'newVersion': new_version
            })
        else:
            return jsonify({'error': 'Failed to create version'}), 400
            
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@api_bp.route('/version/current', methods=['GET'])
def get_current_server_version():
    """Get current server version"""
    try:
        latest = version_manager.get_latest_version()
        return jsonify({
            'currentVersion': latest['versionName'] if latest else '1.0',
            'versionCode': latest['versionCode'] if latest else 1,
            'releaseNotes': latest['releaseNotes'] if latest else '',
            'isForceUpdate': latest.get('isForceUpdate', False) if latest else False
        })
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@api_bp.route('/version/reset', methods=['POST'])
def reset_version():
    """Reset server version to start new cycle"""
    try:
        data = request.get_json()
        target_version = data.get('targetVersion', '1.0')
        reason = data.get('reason', 'Reset to start new version cycle')
        
        # Get current version before reset
        current_version = version_manager.get_latest_version()
        previous_version = current_version['versionName'] if current_version else '1.0'
        
        # Get the base URL from the request
        base_url = request.host_url.rstrip('/')
        download_url = f"{base_url}/api/v1/download/{target_version}"
        
        # Reset to target version
        if version_manager.reset_to_version({
            'versionName': target_version,
            'versionCode': 1,  # Reset version code to 1
            'releaseNotes': f"{reason} - Reset to {target_version}",
            'isForceUpdate': False,
            'downloadUrl': download_url
        }):
            return jsonify({
                'success': True,
                'message': f'Version reset to {target_version} successfully',
                'resetVersion': target_version,
                'previousVersion': previous_version
            })
        else:
            return jsonify({'error': 'Failed to reset version'}), 400
            
    except Exception as e:
        return jsonify({'error': str(e)}), 500 