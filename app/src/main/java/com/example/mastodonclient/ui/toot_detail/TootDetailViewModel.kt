package com.example.mastodonclient.ui.toot_detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.mastodonclient.entity.Toot
import kotlinx.coroutines.CoroutineScope

class TootDetailViewModel(
    private val tootData: Toot?,
    private val coroutineScope: CoroutineScope,
    application: Application
) : AndroidViewModel(application) {

    val toot = MutableLiveData<Toot>().also {
        it.value = tootData
    }
}
