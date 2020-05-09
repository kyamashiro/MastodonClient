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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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
            tootListSnapshot.addAll(tootListResponse)
            tootList.postValue(tootListSnapshot)

            hasNext = tootListResponse.isNotEmpty()
            isLoading.postValue(false)
        }
    }

    fun clear() {
        val tootListSnapshot = tootList.value ?: return
        tootListSnapshot.clear()
    }

    fun delete(toot: Toot) {
        coroutineScope.launch {
            tootRepository.delete(toot.id)

            val tootListSnapshot = tootList.value
            tootListSnapshot?.remove(toot)
            tootList.postValue(tootListSnapshot)
        }
    }

    private suspend fun updateAccountInfo() {
        val accountInfoSnapshot = accountInfo.value
            ?: accountRepository.verifyAccountCredential()

        accountInfo.postValue(accountInfoSnapshot)
    }
}
