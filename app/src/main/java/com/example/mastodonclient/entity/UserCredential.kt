package com.example.mastodonclient.entity

data class UserCredential(
    val instanceUrl: String,
    var username: String? = null,
    var accessToken: String? = null
)
