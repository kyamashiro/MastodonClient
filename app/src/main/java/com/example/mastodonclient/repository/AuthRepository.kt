package com.example.mastodonclient.repository

import com.example.mastodonclient.MastodonApi
import com.example.mastodonclient.entity.ResponseToken
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class AuthRepository(
    instanceUrl: String
) {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val retrofit = Retrofit.Builder()
        .baseUrl(instanceUrl)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
    private val api = retrofit.create(MastodonApi::class.java)

    suspend fun token(
        clientId: String,
        clientSecret: String,
        redirectUri: String,
        scopes: String,
        code: String
    ): ResponseToken = withContext(Dispatchers.IO) {
        return@withContext api.token(
            clientId,
            clientSecret,
            redirectUri,
            scopes,
            code,
            "authorization_code"
        )
    }
}
