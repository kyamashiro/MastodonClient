package com.example.mastodonclient.repository

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaFileRepository(application: Application) {

    private val contentResolver = application.contentResolver

    private val saveDir = application.filesDir

    suspend fun readBitmap(
        mediaUri: Uri
    ): Bitmap = withContext(Dispatchers.IO) {
        @Suppress("DEPRECATION")
        return@withContext MediaStore.Images.Media.getBitmap(
            contentResolver,
            mediaUri
        )
    }

    suspend fun saveBitmap(
        bitmap: Bitmap
    ): File = withContext(Dispatchers.IO) {
        val tempFile = createTempFile(
            directory = saveDir,
            prefix = "media",
            suffix = ".jpg"
        )
        FileOutputStream(tempFile).use {
            bitmap.compress(
                Bitmap.CompressFormat.JPEG, 100, it
            )
        }
        return@withContext tempFile
    }
}
