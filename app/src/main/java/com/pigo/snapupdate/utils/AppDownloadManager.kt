package com.pigo.snapupdate.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.pigo.snapupdate.utils.DownloadUtils.getDownloadStatus
import com.pigo.snapupdate.utils.DownloadUtils.getDownloadUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AppDownloadManager(private val context: Context) {

    suspend fun downloadApk(downloadUrl: String, fileName: String): Long = withContext(Dispatchers.IO) {
        try {
            Logger.i("Starting download: $downloadUrl")
            
            // Validate URL
            if (!downloadUrl.startsWith("http://") && !downloadUrl.startsWith("https://")) {
                throw IllegalArgumentException("Invalid download URL: $downloadUrl. Only HTTP/HTTPS URLs are supported.")
            }
            
            val uri = Uri.parse(downloadUrl)
            val request = DownloadManager.Request(uri)
                .setTitle("Downloading Update")
                .setDescription("Downloading the latest version")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = downloadManager.enqueue(request)
            
            Logger.i("Download enqueued with ID: $downloadId")
            downloadId
        } catch (e: Exception) {
            Logger.e("Error starting download: ${e.message}", e)
            throw e
        }
    }

    /**
     * Validate downloaded APK file
     */
    fun validateDownloadedApk(downloadId: Long): Boolean {
        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)

            if (cursor.moveToFirst()) {
                val status = cursor.getDownloadStatus()
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    val uriString = cursor.getDownloadUri()
                    if (uriString != null) {
                        val file = File(Uri.parse(uriString).path ?: "")
                        if (file.exists()) {
                            // Check if file is a valid APK (at least 1MB and has .apk extension)
                            val isValidApk = file.length() > 1024 * 1024 && file.name.endsWith(".apk")
                            Logger.i("📦 APK validation: ${file.name}, Size: ${file.length()} bytes, Valid: $isValidApk")
                            return isValidApk
                        }
                    }
                }
            }
            cursor.close()
            return false
        } catch (e: Exception) {
            Logger.e("Error validating downloaded APK: ${e.message}", e)
            return false
        }
    }

    fun getDownloadStatus(downloadId: Long): Int {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager.query(query)

        return if (cursor.moveToFirst()) {
            cursor.getDownloadStatus()
        } else {
            DownloadManager.STATUS_FAILED
        }.also {
            cursor.close()
        }
    }

    /**
     * Get downloaded file path
     */
    fun getDownloadedFilePath(downloadId: Long): String? {
        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)

            return if (cursor.moveToFirst()) {
                val uriString = cursor.getDownloadUri()
                if (uriString != null) {
                    Uri.parse(uriString).path
                } else null
            } else null
        } catch (e: Exception) {
            Logger.e("Error getting downloaded file path: ${e.message}", e)
            return null
        }
    }
} 