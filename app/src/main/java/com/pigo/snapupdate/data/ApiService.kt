package com.pigo.snapupdate.data

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Body

interface ApiService {
    @GET("update")
    suspend fun checkForUpdates(@Query("version") currentVersion: String): UpdateResponse

    @GET("health")
    suspend fun healthCheck(): Map<String, Any>

    @GET("versions")
    suspend fun getAllVersions(): Map<String, Any>

    @GET("stats")
    suspend fun getStats(): Map<String, Any>

    @GET("version/current")
    suspend fun getCurrentServerVersion(): ServerVersionInfo

    @POST("version/increment")
    suspend fun incrementVersion(@Body request: VersionIncrementRequest): VersionIncrementResponse

    @POST("version/reset")
    suspend fun resetVersion(@Body request: VersionResetRequest): VersionResetResponse
}

data class ServerVersionInfo(
    val currentVersion: String,
    val versionCode: Int,
    val releaseNotes: String,
    val isForceUpdate: Boolean
)

data class VersionIncrementRequest(
    val version: String,
    val releaseNotes: String = "",
    val isForceUpdate: Boolean = false
)

data class VersionIncrementResponse(
    val success: Boolean,
    val message: String,
    val newVersion: String
)

data class VersionResetRequest(
    val targetVersion: String = "1.0",
    val reason: String = "Reset to start new version cycle"
)

data class VersionResetResponse(
    val success: Boolean,
    val message: String,
    val resetVersion: String,
    val previousVersion: String
) 