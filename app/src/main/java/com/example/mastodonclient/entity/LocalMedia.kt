package com.example.mastodonclient.entity

import android.os.Parcelable
import java.io.File
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LocalMedia(
    val file: File,
    val mediaType: String
) : Parcelable
