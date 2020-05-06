package com.example.mastodonclient.entity

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Toot(
    val id: String,
    val sensitive: Boolean,
    val url: String,
    val content: String,
    @Json(name = "media_attachments") val mediaAttachments: List<Media>,
    val account: Account,
    // JSONのキーとフィールド名を変わる場合などでは、@Json アノテーションを付ける
    @Json(name = "created_at") val createdAt: String
) : Parcelable {
    val topMedia: Media?
        get() = mediaAttachments.firstOrNull()
}
