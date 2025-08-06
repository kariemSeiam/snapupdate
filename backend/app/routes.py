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
        
        # Check if we have a latest version and if it's different from current
        if latest_version and latest_version['versionName'] != current_version:
            # Use GitHub download URL directly
            download_url = latest_version['downloadUrl']
            
            return jsonify({
                'versionCode': latest_version['versionCode'],
                'versionName': latest_version['versionName'],
                'downloadUrl': download_url,
                'releaseNotes': latest_version['releaseNotes'],
                'isForceUpdate': latest_version.get('isForceUpdate', False)
            })
        else:
            # Return 200 with no update message when versions are the same
            return jsonify({
                'message': 'No update available',
                'currentVersion': current_version,
                'latestVersion': latest_version['versionName'] if latest_version else current_version,
                'hasUpdate': False
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
    """Redirect to GitHub download for specific version"""
    try:
        version_info = version_manager.get_version(version)
        if version_info and version_info.get('downloadUrl'):
            # Redirect to GitHub download URL
            return jsonify({
                'redirect': True,
                'downloadUrl': version_info['downloadUrl'],
                'message': f'Redirecting to GitHub download for version {version}'
            }), 302
        else:
            return jsonify({'error': 'Version not found'}), 404
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
    """Increment server version with GitHub download link"""
    try:
        data = request.get_json()
        new_version = data.get('version')
        release_notes = data.get('releaseNotes', '')
        is_force_update = data.get('isForceUpdate', False)
        
        # GitHub download URL will be created in version_manager.add_version()
        
        if version_manager.add_version({
            'versionName': new_version,
            'versionCode': version_manager.get_next_version_code(),
            'releaseNotes': release_notes,
            'isForceUpdate': is_force_update
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
    """Reset server version to start new cycle with GitHub download link"""
    try:
        data = request.get_json()
        target_version = data.get('targetVersion', '1.0')
        reason = data.get('reason', 'Reset to start new version cycle')
        
        # Get current version before reset
        current_version = version_manager.get_latest_version()
        previous_version = current_version['versionName'] if current_version else '1.0'
        
        # GitHub download URL will be created in version_manager.reset_to_version()
        
        # Reset to target version
        if version_manager.reset_to_version({
            'versionName': target_version,
            'versionCode': 1,  # Reset version code to 1
            'releaseNotes': f"{reason} - Reset to {target_version}",
            'isForceUpdate': False
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