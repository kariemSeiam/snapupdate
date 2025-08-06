package com.pigo.snapupdate.utils

import android.util.Log

object Logger {
    private const val TAG = "SnapUpdate"
    
    fun d(message: String) {
        Log.d(TAG, message)
    }
    
    fun i(message: String) {
        Log.i(TAG, message)
    }
    
    fun w(message: String, throwable: Throwable? = null) {
        Log.w(TAG, message, throwable)
    }
    
    fun e(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
    }
    
    fun logDownloadProgress(downloadId: Long, progress: Int) {
        d("Download $downloadId progress: $progress%")
    }
    
    fun logDownloadStatus(downloadId: Long, status: String) {
        d("Download $downloadId status: $status")
    }
    
    fun logInstallationAttempt(filePath: String) {
        i("Attempting to install APK: $filePath")
    }
    
    fun logError(message: String, error: Throwable? = null) {
        e("Error: $message", error)
    }
} 