package com.pigo.snapupdate.ui.viewmodel

import android.app.DownloadManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pigo.snapupdate.data.ApiService
import com.pigo.snapupdate.data.UpdateInfo
import com.pigo.snapupdate.data.ServerVersionInfo
import com.pigo.snapupdate.data.VersionIncrementRequest
import com.pigo.snapupdate.data.VersionResetRequest
import com.pigo.snapupdate.utils.AppDownloadManager
import com.pigo.snapupdate.utils.InstallManager
import com.pigo.snapupdate.utils.DownloadStatus
import com.pigo.snapupdate.utils.DownloadUtils.getDownloadProgress
import com.pigo.snapupdate.utils.DownloadUtils.getDownloadStatus
import com.pigo.snapupdate.utils.Logger
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.io.File
import android.os.Environment
import android.content.pm.PackageManager

data class UpdateUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val updateInfo: UpdateInfo? = null,
    val showUpdateDialog: Boolean = false,
    val showInstallDialog: Boolean = false,
    val downloadProgress: Int = 0,
    val downloadStatus: DownloadStatus = DownloadStatus.Pending,
    val isDownloading: Boolean = false,
    val serverVersion: ServerVersionInfo? = null,
    val isIncrementingVersion: Boolean = false,
    val updateCycleStatus: String = ""
)

class UpdateViewModel(
    private val apiService: ApiService,
    private val downloadManager: AppDownloadManager,
    private val installManager: InstallManager,
    private val context: Context
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(UpdateUiState())
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()
    
    private val currentVersionCode = 1 // Hardcoded for demo
    private val currentVersionName = "1.0" // Hardcoded for demo
    private var currentDownloadId: Long = -1
    
    init {
        Logger.i("🚀 UpdateViewModel initialized")
        startUpdateCycle()
    }
    
    /**
     * 🎯 EXPERT UPDATE CYCLE - Follows backend logic perfectly
     */
    fun startUpdateCycle() {
        viewModelScope.launch {
            try {
                Logger.i("🔄 Starting expert update cycle...")
                _uiState.update { 
                    it.copy(
                        isLoading = true, 
                        error = null,
                        updateCycleStatus = "🔄 Checking for updates..."
                    )
                }
                
                // Step 1: Get server version info
                val serverVersion = apiService.getCurrentServerVersion()
                Logger.i("📊 Server version: ${serverVersion.currentVersion}")
                
                _uiState.update { 
                    it.copy(
                        serverVersion = serverVersion,
                        updateCycleStatus = "📊 Server version: ${serverVersion.currentVersion}"
                    )
                }
                
                // Step 2: Compare versions
                val currentVersion = getAppVersion(context) ?: currentVersionName
                val serverVersionName = serverVersion.currentVersion
                
                Logger.i("🔍 Version comparison: Current=$currentVersion, Server=$serverVersionName")
                _uiState.update { 
                    it.copy(
                        updateCycleStatus = "🔍 Comparing versions: $currentVersion vs $serverVersionName"
                    )
                }
                
                // Step 3: Check if update is needed
                if (isUpdateNeeded(currentVersion, serverVersionName)) {
                    Logger.i("✅ Update needed: $currentVersion -> $serverVersionName")
                    _uiState.update { 
                        it.copy(
                            updateCycleStatus = "✅ Update needed: $currentVersion -> $serverVersionName"
                        )
                    }
                    
                    // Step 4: Check if APK exists in storage
                    val apkFileName = "SnapUpdate-v$serverVersionName.apk"
                    val apkFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), apkFileName)
                    
                    Logger.i("📦 Checking for APK in storage: ${apkFile.absolutePath}")
                    if (apkFile.exists() && apkFile.length() > 1024 * 1024) {
                        Logger.i("📦 APK exists in storage: ${apkFile.absolutePath} (${apkFile.length()} bytes)")
                        _uiState.update { 
                            it.copy(
                                updateCycleStatus = "📦 APK found in storage, installing directly..."
                            )
                        }
                        
                        // Install directly from storage
                        installApkFromStorage(apkFile)
                    } else {
                        Logger.i("📥 APK not found in storage, downloading...")
                        _uiState.update { 
                            it.copy(
                                updateCycleStatus = "📥 APK not found, downloading from GitHub..."
                            )
                        }
                        
                        // Download from GitHub
                        downloadAndInstallUpdate(serverVersionName)
                    }
                } else {
                    Logger.i("✅ No update needed - current version is up to date")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            updateCycleStatus = "✅ No update needed - up to date"
                        )
                    }
                }
                
            } catch (e: Exception) {
                Logger.e("❌ Error in update cycle", e)
                val errorMessage = when {
                    e.message?.contains("Failed to connect") == true -> 
                        "Cannot connect to update server. Please check your internet connection."
                    e.message?.contains("timeout") == true -> 
                        "Connection timeout. Please try again."
                    e.message?.contains("404") == true -> 
                        "Update server not found. Please check server configuration."
                    else -> "Update cycle failed: ${e.message}"
                }
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = errorMessage,
                        updateCycleStatus = "❌ Update cycle failed"
                    )
                }
            }
        }
    }
    
    /**
     * 🎯 Check if update is needed based on version comparison
     */
    private fun isUpdateNeeded(currentVersion: String, serverVersion: String): Boolean {
        return try {
            val currentParts = currentVersion.split(".")
            val serverParts = serverVersion.split(".")
            
            // Compare major version first
            val currentMajor = currentParts[0].toInt()
            val serverMajor = serverParts[0].toInt()
            
            if (currentMajor < serverMajor) return true
            if (currentMajor > serverMajor) return false
            
            // If major versions are equal, compare minor version
            val currentMinor = currentParts[1].toInt()
            val serverMinor = serverParts[1].toInt()
            
            currentMinor < serverMinor
        } catch (e: Exception) {
            Logger.e("❌ Error comparing versions", e)
            false
        }
    }
    
    /**
     * 📦 Install APK directly from storage
     */
    private fun installApkFromStorage(apkFile: File) {
        viewModelScope.launch {
            try {
                Logger.i("🔧 Installing APK from storage: ${apkFile.absolutePath}")
                _uiState.update { 
                    it.copy(
                        updateCycleStatus = "🔧 Installing from storage...",
                        isLoading = false
                    )
                }
                
                // Validate APK file
                if (apkFile.length() < 1024 * 1024) {
                    Logger.e("❌ APK file is too small: ${apkFile.length()} bytes")
                    _uiState.update { 
                        it.copy(
                            error = "Invalid APK file in storage",
                            updateCycleStatus = "❌ Invalid APK in storage"
                        )
                    }
                    return@launch
                }
                
                // Install the APK
                installManager.installApkFile(apkFile)
                
                _uiState.update { 
                    it.copy(
                        updateCycleStatus = "✅ Installation started from storage"
                    )
                }
                
            } catch (e: Exception) {
                Logger.e("❌ Error installing from storage", e)
                _uiState.update { 
                    it.copy(
                        error = "Failed to install from storage: ${e.message}",
                        updateCycleStatus = "❌ Installation failed"
                    )
                }
            }
        }
    }
    
    /**
     * 📥 Download and install update from GitHub
     */
    private fun downloadAndInstallUpdate(serverVersion: String) {
        viewModelScope.launch {
            try {
                Logger.i("📥 Starting download for version: $serverVersion")
                _uiState.update { 
                    it.copy(
                        showInstallDialog = true,
                        isDownloading = true,
                        downloadProgress = 0,
                        downloadStatus = DownloadStatus.Pending,
                        updateCycleStatus = "📥 Downloading from GitHub..."
                    )
                }
                
                // Create GitHub download URL directly
                val githubDownloadUrl = "https://github.com/kariemSeiam/snapupdate/raw/refs/heads/master/backend/data/apks/SnapUpdate-v$serverVersion.apk"
                Logger.i("🎯 Using GitHub download URL: $githubDownloadUrl")
                
                // Start download
                val downloadId = downloadManager.downloadApk(githubDownloadUrl, "SnapUpdate-v$serverVersion.apk")
                currentDownloadId = downloadId
                Logger.i("🚀 Download started with ID: $downloadId")
                
                // Monitor download
                installManager.monitorDownload(downloadId).collect { status ->
                    when (status) {
                        is DownloadStatus.Progress -> {
                            Logger.logDownloadProgress(downloadId, status.percentage)
                            _uiState.update { 
                                it.copy(
                                    downloadProgress = status.percentage,
                                    downloadStatus = status,
                                    updateCycleStatus = "📥 Downloading: ${status.percentage}%"
                                )
                            }
                        }
                        is DownloadStatus.Success -> {
                            Logger.i("🎉 Download completed - installation will start automatically")
                            _uiState.update { 
                                it.copy(
                                    downloadStatus = status,
                                    isDownloading = false,
                                    showInstallDialog = false,
                                    updateCycleStatus = "✅ Download completed, installing..."
                                )
                            }
                        }
                        is DownloadStatus.Failed -> {
                            Logger.e("❌ Download failed: ${status.error}")
                            _uiState.update { 
                                it.copy(
                                    downloadStatus = status,
                                    isDownloading = false,
                                    error = "Download failed: ${status.error}",
                                    updateCycleStatus = "❌ Download failed"
                                )
                            }
                        }
                        is DownloadStatus.Paused -> {
                            Logger.w("⏸️ Download paused")
                            _uiState.update { 
                                it.copy(
                                    downloadStatus = status,
                                    updateCycleStatus = "⏸️ Download paused"
                                )
                            }
                        }
                        is DownloadStatus.Pending -> {
                            Logger.i("⏳ Download pending")
                            _uiState.update { 
                                it.copy(
                                    downloadStatus = status,
                                    updateCycleStatus = "⏳ Download pending"
                                )
                            }
                        }
                    }
                }
                
            } catch (e: Exception) {
                Logger.e("❌ Error during download", e)
                val errorMessage = when {
                    e.message?.contains("Failed to connect") == true -> 
                        "Cannot connect to download server. Please check your internet connection."
                    e.message?.contains("timeout") == true -> 
                        "Download timeout. Please try again."
                    e.message?.contains("404") == true -> 
                        "Download file not found. Please check server configuration."
                    e.message?.contains("Invalid download URL") == true -> 
                        "Invalid download URL. Please check server configuration."
                    else -> "Download failed: ${e.message}"
                }
                _uiState.update { 
                    it.copy(
                        error = errorMessage,
                        isDownloading = false,
                        updateCycleStatus = "❌ Download failed"
                    )
                }
            }
        }
    }
    
    /**
     * 🔄 Manual update check (for user-initiated updates)
     */
    fun checkForUpdates() {
        Logger.i("🔄 Manual update check initiated")
        viewModelScope.launch {
            try {
                Logger.i("🔍 Manual update check...")
                _uiState.update { 
                    it.copy(
                        isLoading = true, 
                        error = null,
                        updateCycleStatus = "🔍 Manual update check..."
                    )
                }
                
                // Get server version info
                val serverVersion = apiService.getCurrentServerVersion()
                val currentVersion = getAppVersion(context) ?: currentVersionName
                val serverVersionName = serverVersion.currentVersion
                
                Logger.i("🔍 Manual check - Current: $currentVersion, Server: $serverVersionName")
                
                if (isUpdateNeeded(currentVersion, serverVersionName)) {
                    Logger.i("✅ Manual check - Update available: $currentVersion -> $serverVersionName")
                    
                    // Create update info for dialog
                    val updateInfo = UpdateInfo(
                        versionCode = serverVersion.versionCode,
                        versionName = serverVersionName,
                        downloadUrl = "https://github.com/kariemSeiam/snapupdate/raw/refs/heads/master/backend/data/apks/SnapUpdate-v$serverVersionName.apk",
                        releaseNotes = serverVersion.releaseNotes,
                        isForceUpdate = serverVersion.isForceUpdate
                    )
                    
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            updateInfo = updateInfo,
                            showUpdateDialog = true,
                            updateCycleStatus = "✅ Update available: $currentVersion -> $serverVersionName"
                        )
                    }
                } else {
                    Logger.i("✅ Manual check - No update needed")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            updateCycleStatus = "✅ No update needed - up to date"
                        )
                    }
                }
                
            } catch (e: Exception) {
                Logger.e("❌ Error in manual update check", e)
                val errorMessage = when {
                    e.message?.contains("Failed to connect") == true -> 
                        "Cannot connect to update server. Please check your internet connection."
                    e.message?.contains("timeout") == true -> 
                        "Connection timeout. Please try again."
                    e.message?.contains("404") == true -> 
                        "Update server not found. Please check server configuration."
                    else -> "Manual update check failed: ${e.message}"
                }
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = errorMessage,
                        updateCycleStatus = "❌ Manual update check failed"
                    )
                }
            }
        }
    }
    
    /**
     * 📊 Get server version info
     */
    fun getServerVersion() {
        viewModelScope.launch {
            try {
                Logger.i("📡 Getting server version...")
                val serverVersion = apiService.getCurrentServerVersion()
                Logger.i("📊 Server version: ${serverVersion.currentVersion}")
                _uiState.update { 
                    it.copy(serverVersion = serverVersion)
                }
            } catch (e: Exception) {
                Logger.e("❌ Error getting server version", e)
            }
        }
    }
    
    /**
     * ⬆️ Increment server version
     */
    fun incrementVersion() {
        viewModelScope.launch {
            try {
                Logger.i("⬆️ Incrementing server version...")
                _uiState.update { it.copy(isIncrementingVersion = true, error = null) }
                
                val currentServerVersion = uiState.value.serverVersion
                val nextVersion = if (currentServerVersion != null) {
                    val parts = currentServerVersion.currentVersion.split(".")
                    val major = parts[0].toInt()
                    val minor = parts[1].toInt()
                    
                    // Check if we've reached the maximum (1.2)
                    if (major == 1 && minor >= 2) {
                        throw Exception("Maximum version reached (1.2). Please reset to start a new cycle.")
                    }
                    
                    "${major}.${minor + 1}"
                } else {
                    "1.1"
                }
                
                val request = VersionIncrementRequest(
                    version = nextVersion,
                    releaseNotes = "Auto-generated version ${nextVersion}",
                    isForceUpdate = false
                )
                
                val response = apiService.incrementVersion(request)
                Logger.i("✅ Version incremented: ${response.newVersion}")
                
                _uiState.update { 
                    it.copy(
                        isIncrementingVersion = false,
                        error = null
                    )
                }
                
                // Refresh server version and start update cycle
                getServerVersion()
                startUpdateCycle()
                
            } catch (e: Exception) {
                Logger.e("❌ Error incrementing version", e)
                _uiState.update { 
                    it.copy(
                        isIncrementingVersion = false,
                        error = "Failed to increment version: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * 🔄 Reset server version
     */
    fun resetVersion() {
        viewModelScope.launch {
            try {
                Logger.i("🔄 Resetting server version to 1.0...")
                _uiState.update { it.copy(isIncrementingVersion = true, error = null) }
                
                val request = VersionResetRequest(
                    targetVersion = "1.0",
                    reason = "Reset to start new version cycle"
                )
                
                val response = apiService.resetVersion(request)
                Logger.i("✅ Version reset to: ${response.resetVersion} (was: ${response.previousVersion})")
                
                _uiState.update { 
                    it.copy(
                        isIncrementingVersion = false,
                        error = null
                    )
                }
                
                // Refresh server version and start update cycle
                getServerVersion()
                startUpdateCycle()
                
            } catch (e: Exception) {
                Logger.e("❌ Error resetting version", e)
                _uiState.update { 
                    it.copy(
                        isIncrementingVersion = false,
                        error = "Failed to reset version: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * 🎯 Accept update (user-initiated from dialog)
     */
    fun acceptUpdate() {
        val updateInfo = uiState.value.updateInfo ?: return
        
        viewModelScope.launch {
            try {
                Logger.i("🎯 User accepted update: ${updateInfo.versionName}")
                _uiState.update { 
                    it.copy(
                        showUpdateDialog = false,
                        showInstallDialog = true,
                        isDownloading = true,
                        downloadProgress = 0,
                        downloadStatus = DownloadStatus.Pending,
                        updateCycleStatus = "📥 Starting download for ${updateInfo.versionName}..."
                    )
                }
                
                // Start download
                val downloadId = downloadManager.downloadApk(updateInfo.downloadUrl, "SnapUpdate-v${updateInfo.versionName}.apk")
                currentDownloadId = downloadId
                Logger.i("🚀 Download started with ID: $downloadId")
                
                // Monitor download
                installManager.monitorDownload(downloadId).collect { status ->
                    when (status) {
                        is DownloadStatus.Progress -> {
                            Logger.logDownloadProgress(downloadId, status.percentage)
                            _uiState.update { 
                                it.copy(
                                    downloadProgress = status.percentage,
                                    downloadStatus = status,
                                    updateCycleStatus = "📥 Downloading: ${status.percentage}%"
                                )
                            }
                        }
                        is DownloadStatus.Success -> {
                            Logger.i("🎉 Download completed - installation will start automatically")
                            _uiState.update { 
                                it.copy(
                                    downloadStatus = status,
                                    isDownloading = false,
                                    showInstallDialog = false,
                                    updateCycleStatus = "✅ Download completed, installing..."
                                )
                            }
                        }
                        is DownloadStatus.Failed -> {
                            Logger.e("❌ Download failed: ${status.error}")
                            _uiState.update { 
                                it.copy(
                                    downloadStatus = status,
                                    isDownloading = false,
                                    error = "Download failed: ${status.error}",
                                    updateCycleStatus = "❌ Download failed"
                                )
                            }
                        }
                        is DownloadStatus.Paused -> {
                            Logger.w("⏸️ Download paused")
                            _uiState.update { 
                                it.copy(
                                    downloadStatus = status,
                                    updateCycleStatus = "⏸️ Download paused"
                                )
                            }
                        }
                        is DownloadStatus.Pending -> {
                            Logger.i("⏳ Download pending")
                            _uiState.update { 
                                it.copy(
                                    downloadStatus = status,
                                    updateCycleStatus = "⏳ Download pending"
                                )
                            }
                        }
                    }
                }
                
            } catch (e: Exception) {
                Logger.e("❌ Error during download", e)
                val errorMessage = when {
                    e.message?.contains("Failed to connect") == true -> 
                        "Cannot connect to download server. Please check your internet connection."
                    e.message?.contains("timeout") == true -> 
                        "Download timeout. Please try again."
                    e.message?.contains("404") == true -> 
                        "Download file not found. Please check server configuration."
                    e.message?.contains("Invalid download URL") == true -> 
                        "Invalid download URL. Please check server configuration."
                    else -> "Download failed: ${e.message}"
                }
                _uiState.update { 
                    it.copy(
                        error = errorMessage,
                        isDownloading = false,
                        updateCycleStatus = "❌ Download failed"
                    )
                }
            }
        }
    }
    
    /**
     * ❌ Dismiss update dialog
     */
    fun dismissUpdate() {
        Logger.i("❌ User dismissed update dialog")
        _uiState.update { it.copy(showUpdateDialog = false) }
    }
    
    /**
     * 🛡️ Dismiss install dialog
     */
    fun dismissInstallDialog() {
        Logger.i("🛡️ User dismissed install dialog")
        _uiState.update { it.copy(showInstallDialog = false) }
    }
    
    /**
     * 🧹 Clear error messages
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * 🧹 Clear success messages
     */
    fun clearSuccessMessage() {
        // No-op as success messages are not tracked in UpdateUiState
    }
    
    /**
     * 🧹 Cleanup on view model destruction
     */
    override fun onCleared() {
        super.onCleared()
        Logger.i("🧹 UpdateViewModel cleared, cleaning up resources")
        installManager.cleanup()
    }
    
    /**
     * Get current app version from package manager
     */
    private fun getAppVersion(context: Context): String? {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }
    
    companion object {
        fun create(context: Context): UpdateViewModel {
            val apiService = com.pigo.snapupdate.data.NetworkModule.apiService
            val downloadManager = AppDownloadManager(context)
            val installManager = InstallManager(context)
            return UpdateViewModel(apiService, downloadManager, installManager, context)
        }
    }
} 