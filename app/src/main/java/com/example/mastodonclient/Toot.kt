package com.example.mastodonclient

import com.squareup.moshi.Json

data class Toot(
    val id: String,
    val sensitive: Boolean,
    val url: String,
    val content: String,
    @Json(name = "media_attachments") val mediaAttachments: List<Media>,
    val account: Account,
    // JSONのキーとフィールド名を変わる場合などでは、@Json アノテーションを付ける
    @Json(name = "created_at") val createdAt: String
) {
    val topMedia: Media?
        get() = mediaAttachments.firstOrNull()
}
