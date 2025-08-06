package com.pigo.snapupdate.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.pigo.snapupdate.utils.DownloadUtils.getDownloadStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
} 