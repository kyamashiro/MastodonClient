package com.example.mastodonclient.ui.toot_list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import com.example.mastodonclient.entity.Account
import com.example.mastodonclient.entity.Toot
import com.example.mastodonclient.entity.UserCredential
import com.example.mastodonclient.repository.AccountRepository
import com.example.mastodonclient.repository.TootRepository
import com.example.mastodonclient.repository.UserCredentialRepository
import java.io.IOException
import java.net.HttpURLConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import retrofit2.HttpException

class TootListViewModel(
    private val instanceUrl: String,
    private val username: String,
    private val timelineType: TimelineType,
    private val coroutineScope: CoroutineScope,
    application: Application
) : AndroidViewModel(application), LifecycleObserver {

    private val userCredentialRepository =
        UserCredentialRepository(
            application
        )
    private lateinit var tootRepository: TootRepository
    private lateinit var accountRepository: AccountRepository

    private lateinit var userCredential: UserCredential

    val loginRequired = MutableLiveData<Boolean>()
    val isLoading = MutableLiveData<Boolean>()
    var hasNext = true
    val accountInfo = MutableLiveData<Account>()
    val tootList = MutableLiveData<ArrayList<Toot>>()
    val errorMessage = MutableLiveData<String>()

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        reloadUserCredential()
    }

    fun reloadUserCredential() {
        coroutineScope.launch {
            val credential = userCredentialRepository
                .find(instanceUrl, username)
            if (credential == null) {
                loginRequired.postValue(true)
                return@launch
            }

            tootRepository = TootRepository(credential)
            accountRepository = AccountRepository(credential)
            userCredential = credential

            clear()
            loadNext()
        }
    }

    fun loadNext() {
        coroutineScope.launch {
            updateAccountInfo()
            isLoading.postValue(true)
            val tootListSnapshot = tootList.value ?: ArrayList()

            val maxId = tootListSnapshot.lastOrNull()?.id
            // timelineTypeによって取得するデータを変更する
            try {
                val tootListResponse = when (timelineType) {
                    TimelineType.PublicTimeline -> {
                        tootRepository.fetchPublicTimeline(
                            maxId = maxId,
                            onlyMedia = true
                        )
                    }
                    TimelineType.HomeTimeline -> {
                        tootRepository.fetchHomeTimeline(
                            maxId = maxId
                        )
                    }
                }

                val newTootList = ArrayList(tootListSnapshot).also {
                    it.addAll(tootListResponse)
                }
                tootList.postValue(newTootList)
                hasNext = tootListResponse.isNotEmpty()
            } catch (e: HttpException) {
                when (e.code()) {
                    HttpURLConnection.HTTP_FORBIDDEN -> {
                        errorMessage.postValue("必要な権限がありません")
                    }
                }
            } catch (e: IOException) {
                errorMessage.postValue(
                    "サーバーに接続できませんでした。${e.message}"
                )
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    fun clear() {
        val tootListSnapshot = tootList.value ?: return
        tootListSnapshot.clear()
    }

    fun delete(toot: Toot) {
        coroutineScope.launch {
            try {
                tootRepository.delete(toot.id)

                val tootListSnapshot = tootList.value ?: ArrayList()
                val newTootList = ArrayList(tootListSnapshot)
                    .also {
                        it.remove(toot)
                    }
                tootList.postValue(newTootList)
            } catch (e: HttpException) {
                when (e.code()) {
                    HttpURLConnection.HTTP_FORBIDDEN -> {
                        errorMessage.postValue("必要な権限がありません")
                    }
                }
            } catch (e: IOException) {
                errorMessage.postValue(
                    "サーバーに接続できませんでした。${e.message}"
                )
            }
        }
    }

    private suspend fun updateAccountInfo() {
        try {
            val accountInfoSnapshot = accountInfo.value
                ?: accountRepository.verifyAccountCredential()

            accountInfo.postValue(accountInfoSnapshot)
        } catch (e: HttpException) {
            when (e.code()) {
                HttpURLConnection.HTTP_FORBIDDEN -> {
                    errorMessage.postValue("必要な権限がありません")
                }
            }
        } catch (e: IOException) {
            errorMessage.postValue(
                "サーバーに接続できませんでした。${e.message}"
            )
        }
    }
}
