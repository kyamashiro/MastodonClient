package com.example.mastodonclient.ui.toot_edit

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.mastodonclient.entity.LocalMedia
import com.example.mastodonclient.repository.MediaFileRepository
import com.example.mastodonclient.repository.TootRepository
import com.example.mastodonclient.repository.UserCredentialRepository
import java.io.IOException
import java.net.HttpURLConnection
import javax.xml.transform.OutputKeys.MEDIA_TYPE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import retrofit2.HttpException

class TootEditViewModel(
    private val instanceUrl: String,
    private val username: String,
    private val coroutineScope: CoroutineScope,
    application: Application
) : AndroidViewModel(application) {

    private val userCredentialRepository = UserCredentialRepository(
        application
    )
    private val mediaFileRepository = MediaFileRepository(application)

    val status = MutableLiveData<String>()
    val postComplete = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val loginRequired = MutableLiveData<Boolean>()

    /*
    * Tootを投稿する　
    * TootEditFragmentから呼び出される
    */
    fun postToot() {
        val statusSnapshot = status.value ?: return
        if (statusSnapshot.isBlank()) {
            errorMessage.postValue("投稿内容がありません")
            return
        }

        coroutineScope.launch {
            val credential = userCredentialRepository.find(instanceUrl, username)
            if (credential == null) {
                loginRequired.postValue(true)
                return@launch
            }
            // Tootを投稿
            val tootRepository = TootRepository(credential)
            try {
                tootRepository.postToot(
                    statusSnapshot
                )
                // 投稿完了フラグ TootEditFragmentで値が監視されている
                postComplete.postValue(true)
            } catch (e: HttpException) {
                when (e.code()) {
                    HttpURLConnection.HTTP_FORBIDDEN -> {
                        errorMessage.postValue("必要な権限がありません")
                    }
                }
            }
        }
    }

    val mediaAttachments = MutableLiveData<ArrayList<LocalMedia>>()

    fun addMedia(mediaUri: Uri) {
        coroutineScope.launch {
            try {
                val bitmap = mediaFileRepository.readBitmap(mediaUri)
                val tempFile = mediaFileRepository.saveBitmap(bitmap)

                val newMediaAttachments = ArrayList<LocalMedia>()
                mediaAttachments.value?.also {
                    newMediaAttachments.addAll(it)
                }
                newMediaAttachments.add(LocalMedia(tempFile, MEDIA_TYPE))
                mediaAttachments.postValue(newMediaAttachments)
            } catch (e: IOException) {
                handleMediaException(mediaUri, e)
            }
        }
    }

    private fun handleMediaException(mediaUri: Uri, e: IOException) {
        errorMessage.postValue("メディアを読み込めません ${e.message} $mediaUri")
    }
}
