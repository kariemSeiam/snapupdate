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
    val isForceUpdate: Boolean = false
) {
    fun hasUpdate(): Boolean {
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