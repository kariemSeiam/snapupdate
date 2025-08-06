package com.pigo.snapupdate.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class PermissionManager(private val context: Context) {
    
    companion object {
        const val REQUEST_INSTALL_PACKAGES_PERMISSION = 1001
        const val REQUEST_STORAGE_PERMISSION = 1002
    }
    
    fun hasInstallPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true // For older versions, permission is granted by default
        }
    }
    
    fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            true // For Android 11+, storage permission is not required for downloads
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    fun requestInstallPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!hasInstallPermission()) {
                Logger.i("Requesting install packages permission")
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                intent.data = Uri.parse("package:${context.packageName}")
                activity.startActivityForResult(intent, REQUEST_INSTALL_PACKAGES_PERMISSION)
            }
        }
    }
    
    fun requestStoragePermission(activity: FragmentActivity): ActivityResultLauncher<String> {
        return activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                Logger.i("Storage permission granted")
            } else {
                Logger.w("Storage permission denied")
            }
        }
    }
    
    fun checkAndRequestPermissions(activity: FragmentActivity): Boolean {
        var allPermissionsGranted = true
        
        // Check storage permission for older Android versions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R && !hasStoragePermission()) {
            allPermissionsGranted = false
            val launcher = requestStoragePermission(activity)
            launcher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        
        // Check install permission for Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !hasInstallPermission()) {
            allPermissionsGranted = false
            requestInstallPermission(activity)
        }
        
        return allPermissionsGranted
    }
    
    fun onActivityResult(requestCode: Int, resultCode: Int): Boolean {
        return when (requestCode) {
            REQUEST_INSTALL_PACKAGES_PERMISSION -> {
                if (resultCode == Activity.RESULT_OK) {
                    Logger.i("Install packages permission granted")
                    true
                } else {
                    Logger.w("Install packages permission denied")
                    false
                }
            }
            REQUEST_STORAGE_PERMISSION -> {
                if (resultCode == Activity.RESULT_OK) {
                    Logger.i("Storage permission granted")
                    true
                } else {
                    Logger.w("Storage permission denied")
                    false
                }
            }
            else -> false
        }
    }
} 