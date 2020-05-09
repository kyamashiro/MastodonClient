package com.example.mastodonclient.ui.toot_edit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.mastodonclient.repository.TootRepository
import com.example.mastodonclient.repository.UserCredentialRepository
import java.net.HttpURLConnection
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
}
