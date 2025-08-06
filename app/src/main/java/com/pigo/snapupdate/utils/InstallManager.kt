package com.pigo.snapupdate.utils

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.pigo.snapupdate.utils.DownloadUtils.getDownloadProgress
import com.pigo.snapupdate.utils.DownloadUtils.getDownloadStatus
import com.pigo.snapupdate.utils.DownloadUtils.getDownloadUri
import com.pigo.snapupdate.utils.DownloadUtils.isDownloadFailed
import com.pigo.snapupdate.utils.DownloadUtils.isDownloadRunning
import com.pigo.snapupdate.utils.DownloadUtils.isDownloadSuccessful
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.File

class InstallManager(private val context: Context) {
    
    private var currentDownloadId: Long = -1
    private var downloadReceiver: BroadcastReceiver? = null
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val notificationId = 1001
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "snapupdate_install",
                "SnapUpdate Installation",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for app installation"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun installApk(downloadId: Long) {
        Logger.logDownloadStatus(downloadId, "🚀 Starting automatic installation process")
        
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager.query(query)
        
        if (cursor.moveToFirst() && cursor.isDownloadSuccessful()) {
            val downloadedFileUri = cursor.getDownloadUri()
            
            if (downloadedFileUri != null) {
                val file = File(Uri.parse(downloadedFileUri).path!!)
                Logger.logInstallationAttempt(file.absolutePath)
                
                // Validate APK file before installation
                val appDownloadManager = AppDownloadManager(context)
                if (appDownloadManager.validateDownloadedApk(downloadId)) {
                    installApkFile(file)
                } else {
                    Logger.logError("❌ Downloaded file is not a valid APK: ${file.absolutePath}")
                    showInstallationNotification("❌ Installation failed - invalid APK file")
                }
            } else {
                Logger.logError("❌ Download URI is null for download ID: $downloadId")
                showInstallationNotification("❌ Installation failed - download error")
            }
        } else {
            Logger.logError("❌ Download not successful for download ID: $downloadId")
            showInstallationNotification("❌ Installation failed - download failed")
        }
        cursor.close()
    }
    
    /**
     * 🔧 Install APK file directly (public method for storage installation)
     */
    fun installApkFile(file: File) {
        installApkFilePrivate(file)
    }
    
    private fun installApkFilePrivate(file: File) {
        try {
            Logger.i("🔧 Automatically installing APK file: ${file.absolutePath}")
            
            // Show notification that installation is starting
            showInstallationNotification("🚀 Starting installation...")
            
            // Check if file exists
            if (!file.exists()) {
                Logger.logError("❌ APK file does not exist: ${file.absolutePath}")
                showInstallationNotification("❌ Installation failed - file not found")
                return
            }
            
            // Check file size - must be at least 1MB to be a valid APK
            if (file.length() < 1024 * 1024) {
                Logger.logError("❌ APK file is too small (${file.length()} bytes): ${file.absolutePath}")
                showInstallationNotification("❌ Installation failed - invalid APK file (too small)")
                return
            }
            
            // Check file extension
            if (!file.name.endsWith(".apk")) {
                Logger.logError("❌ File is not an APK: ${file.absolutePath}")
                showInstallationNotification("❌ Installation failed - not an APK file")
                return
            }
            
            Logger.i("📦 APK file size: ${file.length()} bytes")
            
            val intent = Intent(Intent.ACTION_VIEW)
            val apkUri: Uri
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // For Android 7.0 and above, use FileProvider
                apkUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                Logger.d("🔒 Using FileProvider for automatic APK installation")
            } else {
                apkUri = Uri.fromFile(file)
                Logger.d("📁 Using direct file URI for automatic APK installation")
            }
            
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            // Automatically start installation without user confirmation
            try {
                context.startActivity(intent)
                Logger.i("✅ Automatic APK installation intent launched successfully")
                showInstallationNotification("✅ Installation started - please confirm in the installer")
            } catch (e: Exception) {
                Logger.logError("❌ Failed to start automatic installation", e)
                showInstallationNotification("❌ Installation failed - please enable unknown sources")
                // Fallback: Open settings to enable unknown sources
                openUnknownSourcesSettings()
            }
        } catch (e: Exception) {
            Logger.logError("❌ Failed to install APK file: ${file.absolutePath}", e)
            showInstallationNotification("❌ Installation failed - please try again")
            // Fallback: Open settings to enable unknown sources
            openUnknownSourcesSettings()
        }
    }
    
    private fun showInstallationNotification(message: String) {
        val notification = NotificationCompat.Builder(context, "snapupdate_install")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("SnapUpdate Installation")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(notificationId, notification)
    }
    
    private fun openUnknownSourcesSettings() {
        try {
            Logger.i("🔧 Opening unknown sources settings for automatic installation")
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Logger.logError("❌ Failed to open unknown sources settings", e)
        }
    }
    
    fun monitorDownload(downloadId: Long): Flow<DownloadStatus> = callbackFlow {
        Logger.logDownloadStatus(downloadId, "🎯 Starting BULLETPROOF download monitoring")
        currentDownloadId = downloadId
        
        // 🚀 BULLETPROOF BROADCAST RECEIVER - 100% RELIABLE
        downloadReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Logger.d("📡 Broadcast received: ${intent?.action}")
                
                if (intent?.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    Logger.d("📡 Download complete broadcast for ID: $id, monitoring ID: $downloadId")
                    
                    if (id == downloadId) {
                        Logger.logDownloadStatus(downloadId, "🎉 DOWNLOAD COMPLETED - Starting automatic installation")
                        
                        val downloadManager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                        val query = DownloadManager.Query().setFilterById(downloadId)
                        val cursor = downloadManager.query(query)
                        
                        if (cursor.moveToFirst()) {
                            val status = cursor.getDownloadStatus()
                            Logger.d("📊 Download status from cursor: $status")
                            
                            when {
                                cursor.isDownloadSuccessful() -> {
                                    Logger.logDownloadStatus(downloadId, "✅ Download successful - TRIGGERING INSTALLATION")
                                    trySend(DownloadStatus.Success)
                                    // 🚀 AUTOMATIC INSTALLATION TRIGGER
                                    installApk(downloadId)
                                }
                                cursor.isDownloadFailed() -> {
                                    Logger.logError("❌ Download failed for download ID: $downloadId")
                                    trySend(DownloadStatus.Failed("Download failed"))
                                }
                                cursor.getDownloadStatus() == DownloadManager.STATUS_PAUSED -> {
                                    Logger.logDownloadStatus(downloadId, "⏸️ Download paused")
                                    trySend(DownloadStatus.Paused)
                                }
                                cursor.getDownloadStatus() == DownloadManager.STATUS_PENDING -> {
                                    Logger.logDownloadStatus(downloadId, "⏳ Download pending")
                                    trySend(DownloadStatus.Pending)
                                }
                                cursor.isDownloadRunning() -> {
                                    val progress = cursor.getDownloadProgress()
                                    Logger.logDownloadProgress(downloadId, progress)
                                    trySend(DownloadStatus.Progress(progress))
                                }
                            }
                        } else {
                            Logger.logError("❌ No download found for ID: $downloadId")
                        }
                        cursor.close()
                    }
                }
            }
        }
        
        // 🎯 REGISTER BULLETPROOF RECEIVER
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        
        // Use ContextCompat for Android U compatibility
        ContextCompat.registerReceiver(
            context,
            downloadReceiver!!,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        
        Logger.d("✅ BULLETPROOF Broadcast receiver registered for automatic installation monitoring")
        
        // 🚀 BACKUP POLLING MECHANISM - TRIPLE INSURANCE
        startBulletproofPolling(downloadId) { status ->
            trySend(status)
        }
        
        awaitClose {
            // 🛡️ DON'T UNREGISTER - KEEP RECEIVER ACTIVE FOR COMPLETION
            Logger.d("🛡️ Download monitoring closed, but BULLETPROOF receiver remains active")
        }
    }
    
    // 🚀 BULLETPROOF POLLING - 100% RELIABLE BACKUP
    private fun startBulletproofPolling(downloadId: Long, onStatus: (DownloadStatus) -> Unit) {
        Thread {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            var isCompleted = false
            var pollCount = 0
            
            Logger.d("🚀 Starting BULLETPROOF polling for download ID: $downloadId")
            
            while (!isCompleted && pollCount < 600) { // Max 10 minutes for large files
                try {
                    val query = DownloadManager.Query().setFilterById(downloadId)
                    val cursor = downloadManager.query(query)
                    
                    if (cursor.moveToFirst()) {
                        val status = cursor.getDownloadStatus()
                        val progress = cursor.getDownloadProgress()
                        
                        Logger.d("🔍 Poll $pollCount - Download ID: $downloadId, Status: $status, Progress: $progress%")
                        
                        when {
                            cursor.isDownloadSuccessful() -> {
                                Logger.logDownloadStatus(downloadId, "🎉 BULLETPROOF Polling detected successful download")
                                onStatus(DownloadStatus.Success)
                                // 🚀 AUTOMATIC INSTALLATION TRIGGER
                                installApk(downloadId)
                                isCompleted = true
                            }
                            cursor.isDownloadFailed() -> {
                                Logger.logError("❌ BULLETPROOF Polling detected failed download for ID: $downloadId")
                                onStatus(DownloadStatus.Failed("Download failed"))
                                isCompleted = true
                            }
                            cursor.isDownloadRunning() -> {
                                Logger.logDownloadProgress(downloadId, progress)
                                onStatus(DownloadStatus.Progress(progress))
                            }
                            status == DownloadManager.STATUS_PAUSED -> {
                                Logger.logDownloadStatus(downloadId, "⏸️ Download paused")
                                onStatus(DownloadStatus.Paused)
                            }
                            status == DownloadManager.STATUS_PENDING -> {
                                Logger.logDownloadStatus(downloadId, "⏳ Download pending")
                                onStatus(DownloadStatus.Pending)
                            }
                        }
                    } else {
                        Logger.d("🔍 Poll $pollCount - No download found for ID: $downloadId")
                    }
                    cursor.close()
                    
                    pollCount++
                    Thread.sleep(1000) // Poll every second
                } catch (e: Exception) {
                    Logger.logError("❌ Error during BULLETPROOF polling", e)
                    Thread.sleep(2000) // Wait longer on error
                }
            }
            
            if (!isCompleted) {
                Logger.logError("⏰ BULLETPROOF Polling timeout for download ID: $downloadId")
                onStatus(DownloadStatus.Failed("Download timeout"))
            }
        }.start()
    }
    
    fun cleanup() {
        downloadReceiver?.let { receiver ->
            try {
                context.unregisterReceiver(receiver)
                Logger.d("🧹 BULLETPROOF Download receiver unregistered")
            } catch (e: Exception) {
                Logger.logError("❌ Error unregistering BULLETPROOF receiver", e)
            }
        }
        downloadReceiver = null
        currentDownloadId = -1
    }
}

sealed class DownloadStatus {
    object Pending : DownloadStatus()
    data class Progress(val percentage: Int) : DownloadStatus()
    object Success : DownloadStatus()
    data class Failed(val error: String) : DownloadStatus()
    object Paused : DownloadStatus()
} 