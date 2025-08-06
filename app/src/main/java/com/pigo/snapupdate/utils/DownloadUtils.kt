package com.pigo.snapupdate.utils

import android.app.DownloadManager
import android.database.Cursor

object DownloadUtils {
    
    /**
     * Safely get column index and return a default value if not found
     */
    inline fun <T> Cursor.getColumnValue(
        columnName: String,
        defaultValue: T,
        getter: (Int) -> T
    ): T {
        val columnIndex = getColumnIndex(columnName)
        return if (columnIndex >= 0) {
            getter(columnIndex)
        } else {
            defaultValue
        }
    }
    
    /**
     * Get download status safely
     */
    fun Cursor.getDownloadStatus(): Int {
        return getColumnValue(
            columnName = DownloadManager.COLUMN_STATUS,
            defaultValue = DownloadManager.STATUS_FAILED
        ) { getInt(it) }
    }
    
    /**
     * Get download progress safely
     */
    fun Cursor.getDownloadProgress(): Int {
        val bytesDownloaded = getColumnValue(
            columnName = DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR,
            defaultValue = 0L
        ) { getLong(it) }
        
        val bytesTotal = getColumnValue(
            columnName = DownloadManager.COLUMN_TOTAL_SIZE_BYTES,
            defaultValue = 0L
        ) { getLong(it) }
        
        return if (bytesTotal > 0) {
            (bytesDownloaded * 100 / bytesTotal).toInt()
        } else {
            0
        }
    }
    
    /**
     * Get download URI safely
     */
    fun Cursor.getDownloadUri(): String? {
        return getColumnValue(
            columnName = DownloadManager.COLUMN_LOCAL_URI,
            defaultValue = null
        ) { getString(it) }
    }
    
    /**
     * Check if download is successful
     */
    fun Cursor.isDownloadSuccessful(): Boolean {
        return getDownloadStatus() == DownloadManager.STATUS_SUCCESSFUL
    }
    
    /**
     * Check if download failed
     */
    fun Cursor.isDownloadFailed(): Boolean {
        return getDownloadStatus() == DownloadManager.STATUS_FAILED
    }
    
    /**
     * Check if download is running
     */
    fun Cursor.isDownloadRunning(): Boolean {
        return getDownloadStatus() == DownloadManager.STATUS_RUNNING
    }
} 