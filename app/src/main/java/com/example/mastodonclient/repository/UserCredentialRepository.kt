package com.example.mastodonclient.repository

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.content.edit
import com.example.mastodonclient.entity.UserCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserCredentialRepository(
    private val application: Application
) {
    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
    }

    suspend fun set(
        userCredential: UserCredential
    ) = withContext(Dispatchers.IO) {
        val pref = getPreference(userCredential.instanceUrl)
        pref?.edit {
            putString(KEY_ACCESS_TOKEN, userCredential.accessToken)
        }
    }

    suspend fun find(
        instanceUrl: String,
        username: String
    ): UserCredential? = withContext(Dispatchers.Main) {
        val pref = getPreference(instanceUrl)
            ?: return@withContext null
        val accessToken = pref.getString(KEY_ACCESS_TOKEN, null)
            ?: return@withContext null
        return@withContext UserCredential(instanceUrl, username, accessToken)
    }

    private fun getPreference(instanceUrl: String): SharedPreferences? {
        val hostname = Uri.parse(instanceUrl).host
            ?: return null
        val filename = "{$hostname}.dat"
        return application.getSharedPreferences(
            filename,
            Context.MODE_PRIVATE
        )
    }
}
