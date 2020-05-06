package com.example.mastodonclient

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

// parcelableでオブジェクトを一時的に保存する. Serializeみたいな
@Parcelize
data class Account(
    val id: String,
    val username: String,
    @Json(name = "display_name") val displayName: String,
    val url: String
) : Parcelable
