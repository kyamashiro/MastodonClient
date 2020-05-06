package com.example.mastodonclient.ui

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.mastodonclient.MastodonApi
import com.example.mastodonclient.R
import com.example.mastodonclient.databinding.FragmentMainBinding
import com.example.mastodonclient.entity.Toot
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class MainFragment : Fragment(R.layout.fragment_main) {
    // singleton object
    companion object {
        private val TAG = MainFragment::class.java.simpleName
        private const val API_BASE_URL = "https://androidbook2020.keiji.io"
    }

    // JSON Parser
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val retrofit = Retrofit.Builder()
        .baseUrl(API_BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
    private val api = retrofit.create(MastodonApi::class.java)

    private var binding: FragmentMainBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)
        // buttonがクリックされたときにtextの値を変える
        binding?.button?.setOnClickListener {
            binding?.button?.text = "clicked"
            // メインスレッドでhttp通信できないのでcoroutineを使用する
            // https://shirusu-ni-tarazu.hatenablog.jp/entry/2013/01/20/033030
            CoroutineScope(Dispatchers.IO).launch {
                // APIでTootListを取得
                val tootList = api.fetchPublicTimeline()
                showTootList(tootList)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding?.unbind()
    }

    private suspend fun showTootList(tootList: List<Toot>) = withContext(Dispatchers.Main) {
        val binding = binding ?: return@withContext
        // displayNameは空文字なのでusernameを使用した
        val accountNameList: List<String> = tootList.map {
            it.account.username
        }
        binding.button.text = accountNameList.joinToString("\n")
    }
}
