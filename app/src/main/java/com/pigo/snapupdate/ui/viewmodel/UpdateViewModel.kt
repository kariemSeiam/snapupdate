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
    val isIncrementingVersion: Boolean = false
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
    private var currentDownloadId: Long = -1
    
    init {
        Logger.i("🚀 UpdateViewModel initialized")
        checkForUpdates()
        getServerVersion()
    }
    
    fun checkForUpdates() {
        viewModelScope.launch {
            try {
                Logger.i("🔍 Checking for updates...")
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                val updateResponse = apiService.checkForUpdates()
                Logger.i("📡 Received update response: ${updateResponse.message ?: "Update available"}")
                
                if (updateResponse.hasUpdate()) {
                    val updateInfo = updateResponse.toUpdateInfo()
                    Logger.i("🎯 Update available: ${updateInfo?.versionName}")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            updateInfo = updateInfo,
                            showUpdateDialog = true
                        )
                    }
                } else {
                    Logger.i("✅ No update available")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            updateInfo = null
                        )
                    }
                }
            } catch (e: Exception) {
                Logger.e("❌ Error checking for updates", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to check for updates: ${e.message}"
                    )
                }
            }
        }
    }
    
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
                    
                    // Check if we've reached the maximum (1.3)
                    if (major == 1 && minor >= 3) {
                        throw Exception("Maximum version reached (1.3). Please reset to start a new cycle.")
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
                
                // Refresh server version and check for updates
                getServerVersion()
                checkForUpdates()
                
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
                
                // Refresh server version and check for updates
                getServerVersion()
                checkForUpdates()
                
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
                        downloadStatus = DownloadStatus.Pending
                    )
                }
                
                // 🚀 Start download
                val downloadId = downloadManager.downloadApk(updateInfo.downloadUrl, "SnapUpdate-${updateInfo.versionName}.apk")
                currentDownloadId = downloadId
                Logger.i("🚀 Download started with ID: $downloadId")
                
                // 🛡️ TRIPLE INSURANCE - Direct monitoring as backup
                startBulletproofDirectMonitoring(downloadId)
                
                // 🎯 BULLETPROOF MONITORING - Main system
                installManager.monitorDownload(downloadId).collect { status ->
                    when (status) {
                        is DownloadStatus.Progress -> {
                            Logger.logDownloadProgress(downloadId, status.percentage)
                            _uiState.update { 
                                it.copy(
                                    downloadProgress = status.percentage,
                                    downloadStatus = status
                                )
                            }
                        }
                        is DownloadStatus.Success -> {
                            Logger.i("🎉 Download completed successfully - INSTALLATION WILL START AUTOMATICALLY")
                            _uiState.update { 
                                it.copy(
                                    downloadStatus = status,
                                    isDownloading = false,
                                    showInstallDialog = false // Automatically close dialog
                                )
                            }
                            // 🚀 Installation will be automatically triggered by InstallManager
                        }
                        is DownloadStatus.Failed -> {
                            Logger.e("❌ Download failed: ${status.error}")
                            _uiState.update { 
                                it.copy(
                                    downloadStatus = status,
                                    isDownloading = false
                                )
                            }
                        }
                        is DownloadStatus.Paused -> {
                            Logger.w("⏸️ Download paused")
                            _uiState.update { 
                                it.copy(
                                    downloadStatus = status
                                )
                            }
                        }
                        is DownloadStatus.Pending -> {
                            Logger.i("⏳ Download pending")
                            _uiState.update { 
                                it.copy(
                                    downloadStatus = status
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Logger.e("❌ Error during download", e)
                _uiState.update { 
                    it.copy(
                        error = "Download failed: ${e.message}",
                        isDownloading = false
                    )
                }
            }
        }
    }
    
    // 🛡️ BULLETPROOF DIRECT MONITORING - TRIPLE INSURANCE
    private fun startBulletproofDirectMonitoring(downloadId: Long) {
        viewModelScope.launch {
            Logger.i("🛡️ Starting BULLETPROOF direct monitoring for ID: $downloadId")
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            var isCompleted = false
            var checkCount = 0
            
            while (!isCompleted && checkCount < 600) { // Max 10 minutes for large files
                try {
                    val query = DownloadManager.Query().setFilterById(downloadId)
                    val cursor = downloadManager.query(query)
                    
                    if (cursor.moveToFirst()) {
                        val status = cursor.getDownloadStatus()
                        val progress = cursor.getDownloadProgress()
                        
                        Logger.d("🛡️ Direct check $checkCount - Download ID: $downloadId, Status: $status, Progress: $progress%")
                        
                        when (status) {
                            DownloadManager.STATUS_SUCCESSFUL -> {
                                Logger.i("🎉 BULLETPROOF Direct monitoring detected successful download")
                                installManager.installApk(downloadId)
                                isCompleted = true
                            }
                            DownloadManager.STATUS_FAILED -> {
                                Logger.e("❌ BULLETPROOF Direct monitoring detected failed download")
                                isCompleted = true
                            }
                            DownloadManager.STATUS_RUNNING -> {
                                // Update progress
                                _uiState.update { 
                                    it.copy(
                                        downloadProgress = progress,
                                        downloadStatus = DownloadStatus.Progress(progress)
                                    )
                                }
                            }
                        }
                    }
                    cursor.close()
                    
                    checkCount++
                    delay(2000) // Check every 2 seconds
                } catch (e: Exception) {
                    Logger.logError("❌ Error during BULLETPROOF direct monitoring", e)
                    delay(3000) // Wait longer on error
                }
            }
            
            if (!isCompleted) {
                Logger.logError("⏰ BULLETPROOF Direct monitoring timeout for download ID: $downloadId")
            }
        }
    }
    
    fun dismissUpdate() {
        Logger.i("❌ User dismissed update dialog")
        _uiState.update { it.copy(showUpdateDialog = false) }
    }
    
    fun dismissInstallDialog() {
        Logger.i("🛡️ User dismissed install dialog - but BULLETPROOF download continues in background")
        _uiState.update { it.copy(showInstallDialog = false) }
        // 🚀 DON'T STOP THE DOWNLOAD - Let it continue and install automatically
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    override fun onCleared() {
        super.onCleared()
        Logger.i("🧹 UpdateViewModel cleared, cleaning up BULLETPROOF resources")
        installManager.cleanup()
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