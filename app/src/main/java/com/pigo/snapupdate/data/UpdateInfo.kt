package com.pigo.snapupdate.data

data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val downloadUrl: String,
    val releaseNotes: String,
    val isForceUpdate: Boolean = false
)

data class UpdateResponse(
    val message: String? = null,
    val currentVersion: String? = null,
    val latestVersion: String? = null,
    val versionCode: Int? = null,
    val versionName: String? = null,
    val downloadUrl: String? = null,
    val releaseNotes: String? = null,
    val isForceUpdate: Boolean = false,
    val hasUpdate: Boolean = false
) {
    fun hasUpdate(): Boolean {
        // Check if the backend explicitly says there's no update
        if (hasUpdate == false) {
            return false
        }
        // Otherwise check if we have the required fields for an update
        return versionCode != null && versionName != null && downloadUrl != null
    }
    
    fun toUpdateInfo(): UpdateInfo? {
        return if (hasUpdate()) {
            UpdateInfo(
                versionCode = versionCode!!,
                versionName = versionName!!,
                downloadUrl = downloadUrl!!,
                releaseNotes = releaseNotes ?: "",
                isForceUpdate = isForceUpdate
            )
        } else null
    }
} 