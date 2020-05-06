package com.example.mastodonclient.repository

import android.app.Application
import com.example.mastodonclient.BuildConfig
import com.example.mastodonclient.entity.UserCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserCredentialRepository(
    private val application: Application
) {

    suspend fun find(
        instanceUrl: String,
        username: String
    ): UserCredential? = withContext(Dispatchers.IO) {

        return@withContext UserCredential(
            BuildConfig.INSTANCE_URL,
            BuildConfig.USERNAME,
            BuildConfig.ACCESS_TOKEN
        )
    }
}