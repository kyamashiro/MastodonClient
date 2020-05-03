package com.example.mastodonclient

import com.squareup.moshi.Json

data class Toot(
    val id: String,
    // JSONのキーとフィールド名を変わる場合などでは、@Json アノテーションを付ける
    @Json(name = "created_at") val createdAt: String,
    val sensitive: Boolean,
    val url: String,
    val content: String,
    val account: Account
)
