"""
Version Manager for SnapUpdate Backend
Handles version data and APK file management
"""

import os
import json
from datetime import datetime
from typing import Dict, List, Optional

class VersionManager:
    """Manages app versions and APK files"""
    
    def __init__(self):
        self.data_dir = os.path.join(os.path.dirname(__file__), 'versions')
        self.apk_dir = os.path.join(os.path.dirname(__file__), 'apks')
        self.stats_file = os.path.join(os.path.dirname(__file__), 'stats.json')
        os.makedirs(self.data_dir, exist_ok=True)
        os.makedirs(self.apk_dir, exist_ok=True)
        self._init_demo_data()
    
    def _init_demo_data(self):
        """Initialize demo version data with GitHub download links"""
        self.demo_versions = {
            "1.0": {
                "versionCode": 1,
                "versionName": "1.0",
                "releaseNotes": "Initial release with basic update functionality",
                "downloadUrl": "https://github.com/kariemSeiam/snapupdate/raw/refs/heads/master/backend/data/apks/SnapUpdate-v1.0.apk",
                "isForceUpdate": False,
                "createdAt": "2024-01-01T00:00:00Z"
            },
            "1.1": {
                "versionCode": 2,
                "versionName": "1.1",
                "releaseNotes": "Enhanced UI/UX with Material 3 design",
                "downloadUrl": "https://github.com/kariemSeiam/snapupdate/raw/refs/heads/master/backend/data/apks/SnapUpdate-v1.1.apk",
                "isForceUpdate": False,
                "createdAt": "2024-01-15T00:00:00Z"
            },
            "1.2": {
                "versionCode": 3,
                "versionName": "1.2",
                "releaseNotes": "Added auto-installation feature and improved performance",
                "downloadUrl": "https://github.com/kariemSeiam/snapupdate/raw/refs/heads/master/backend/data/apks/SnapUpdate-v1.2.apk",
                "isForceUpdate": True,
                "createdAt": "2024-01-30T00:00:00Z"
            }
        }
        self._save_versions()
        # Remove auto-creation of APK files - keep them static
        print("✅ Version manager initialized with GitHub download links")
    
    def _create_demo_apks(self):
        """This method is now disabled - APK files are kept static"""
        print("ℹ️ APK files are kept static - no auto-creation")
        pass
    
    def _save_versions(self):
        """Save versions to JSON file"""
        versions_file = os.path.join(self.data_dir, 'versions.json')
        with open(versions_file, 'w') as f:
            json.dump(self.demo_versions, f, indent=2)
    
    def get_latest_version(self) -> Dict:
        """Get the latest version available"""
        versions = self.get_all_versions()
        if versions:
            return max(versions, key=lambda x: x['versionCode'])
        return None
    
    def get_all_versions(self) -> List[Dict]:
        """Get all available versions"""
        versions_file = os.path.join(self.data_dir, 'versions.json')
        if os.path.exists(versions_file):
            with open(versions_file, 'r') as f:
                versions_data = json.load(f)
                return list(versions_data.values())
        return list(self.demo_versions.values())
    
    def get_version(self, version_name: str) -> Optional[Dict]:
        """Get specific version by name"""
        versions = self.get_all_versions()
        return next((v for v in versions if v['versionName'] == version_name), None)
    
    def get_apk_path(self, version: str) -> str:
        """Get APK file path for version"""
        return os.path.join(self.apk_dir, f"SnapUpdate-v{version}.apk")
    
    def get_all_available_apks(self) -> List[str]:
        """Get all available APK files on server (regardless of version management)"""
        try:
            apk_files = []
            if os.path.exists(self.apk_dir):
                for file in os.listdir(self.apk_dir):
                    if file.endswith('.apk'):
                        # Extract version from filename (e.g., "SnapUpdate-v1.0.apk" -> "1.0")
                        version = file.replace('SnapUpdate-v', '').replace('.apk', '')
                        apk_files.append(version)
            return sorted(apk_files)
        except Exception as e:
            print(f"Error getting available APKs: {e}")
            return []
    
    def apk_exists(self, version: str) -> bool:
        """Check if APK file exists for version"""
        apk_path = self.get_apk_path(version)
        return os.path.exists(apk_path)
    
    def get_next_version_code(self) -> int:
        """Get next version code"""
        latest = self.get_latest_version()
        return (latest['versionCode'] + 1) if latest else 1
    
    def add_version(self, version_data: Dict) -> bool:
        """Add new version with GitHub download link"""
        try:
            versions_file = os.path.join(self.data_dir, 'versions.json')
            versions_data = {}
            
            if os.path.exists(versions_file):
                with open(versions_file, 'r') as f:
                    versions_data = json.load(f)
            
            version_name = version_data['versionName']
            
            # Create GitHub download link
            github_download_url = f"https://github.com/kariemSeiam/snapupdate/raw/refs/heads/master/backend/data/apks/SnapUpdate-v{version_name}.apk"
            
            versions_data[version_name] = {
                **version_data,
                'downloadUrl': github_download_url,
                'createdAt': datetime.now().isoformat() + 'Z'
            }
            
            with open(versions_file, 'w') as f:
                json.dump(versions_data, f, indent=2)
            
            # Don't create local APK files - keep them static
            print(f"✅ Added version {version_name} with GitHub download link: {github_download_url}")
            
            self.increment_stat('versions_created')
            return True
        except Exception as e:
            print(f"Error adding version: {e}")
            return False
    
    def update_version(self, version_name: str, version_data: Dict) -> bool:
        """Update existing version"""
        try:
            versions_file = os.path.join(self.data_dir, 'versions.json')
            if os.path.exists(versions_file):
                with open(versions_file, 'r') as f:
                    versions_data = json.load(f)
                
                if version_name in versions_data:
                    versions_data[version_name].update(version_data)
                    versions_data[version_name]['updatedAt'] = datetime.now().isoformat() + 'Z'
                    
                    with open(versions_file, 'w') as f:
                        json.dump(versions_data, f, indent=2)
                    
                    self.increment_stat('versions_updated')
                    return True
            return False
        except Exception as e:
            print(f"Error updating version: {e}")
            return False
    
    def delete_version(self, version_name: str) -> bool:
        """Delete version"""
        try:
            versions_file = os.path.join(self.data_dir, 'versions.json')
            if os.path.exists(versions_file):
                with open(versions_file, 'r') as f:
                    versions_data = json.load(f)
                
                if version_name in versions_data:
                    del versions_data[version_name]
                    
                    with open(versions_file, 'w') as f:
                        json.dump(versions_data, f, indent=2)
                    
                    # Delete APK file
                    apk_path = self.get_apk_path(version_name)
                    if os.path.exists(apk_path):
                        os.remove(apk_path)
                    
                    self.increment_stat('versions_deleted')
                    return True
            return False
        except Exception as e:
            print(f"Error deleting version: {e}")
            return False
    
    def reset_to_version(self, version_data: Dict) -> bool:
        """Reset to specific version with GitHub download link"""
        try:
            versions_file = os.path.join(self.data_dir, 'versions.json')
            version_name = version_data['versionName']
            
            # Create GitHub download link
            github_download_url = f"https://github.com/kariemSeiam/snapupdate/raw/refs/heads/master/backend/data/apks/SnapUpdate-v{version_name}.apk"
            
            # Clear all existing versions and create new reset version
            versions_data = {
                version_name: {
                    **version_data,
                    'downloadUrl': github_download_url,
                    'createdAt': datetime.now().isoformat() + 'Z'
                }
            }
            
            with open(versions_file, 'w') as f:
                json.dump(versions_data, f, indent=2)
            
            # Don't create local APK files - keep them static
            print(f"✅ Reset to version {version_name} with GitHub download link: {github_download_url}")
            
            self.increment_stat('versions_reset')
            return True
        except Exception as e:
            print(f"Error resetting to version: {e}")
            return False
    
    def get_stats(self) -> Dict:
        """Get server statistics"""
        if os.path.exists(self.stats_file):
            with open(self.stats_file, 'r') as f:
                return json.load(f)
        return {
            'total_versions': len(self.get_all_versions()),
            'versions_created': 0,
            'versions_updated': 0,
            'versions_deleted': 0,
            'downloads': 0,
            'last_updated': datetime.now().isoformat()
        }
    
    def increment_stat(self, stat_name: str):
        """Increment statistics counter"""
        stats = self.get_stats()
        stats[stat_name] = stats.get(stat_name, 0) + 1
        stats['last_updated'] = datetime.now().isoformat()
        
        with open(self.stats_file, 'w') as f:
            json.dump(stats, f, indent=2) 