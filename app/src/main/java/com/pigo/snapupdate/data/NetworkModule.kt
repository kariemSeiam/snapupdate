package com.pigo.snapupdate.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    // Use local IP address for development, remote server for production
    private const val LOCAL_BASE_URL = "http://192.168.1.202:5000/api/v1/"
    private const val REMOTE_BASE_URL = "https://geolink.pythonanywhere.com/api/v1/"
    
    // For now, use local server - change to REMOTE_BASE_URL for production
    private const val BASE_URL = REMOTE_BASE_URL
        
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val apiService: ApiService = retrofit.create(ApiService::class.java)
} 