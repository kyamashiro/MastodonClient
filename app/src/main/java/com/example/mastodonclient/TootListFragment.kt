package com.example.mastodonclient

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mastodonclient.databinding.FragmentTootListBinding
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class TootListFragment : Fragment(R.layout.fragment_toot_list) {
    // singleton API
    companion object {
        val TAG = TootListFragment::class.java.simpleName
        private const val API_BASE_URL = "https://androidbook2020.keiji.io"
    }

    private var binding: FragmentTootListBinding? = null

    // JSON Parser
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // http client
    private val retrofit = Retrofit.Builder()
        .baseUrl(API_BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
    private val api = retrofit.create(MastodonApi::class.java)

    // 遅延初期化
    private lateinit var adapter: TootListAdapter
    private lateinit var layoutManager: LinearLayoutManager

    // 取得したTootをセットするためのList
    private val tootList = ArrayList<Toot>()

    // スレッドセーフなboolean
    private var isLoading = AtomicBoolean()
    private var hasNext = AtomicBoolean().apply { set(true) }

    // Scrollでの読み込み動作
    private val loadNextScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            // loading中, 次のデータがない場合は読み込みしない
            if (isLoading.get() || !hasNext.get()) {
                return
            }

            val visibleItemCount = recyclerView.childCount
            val totalItemCount = layoutManager.itemCount
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

            if ((totalItemCount - visibleItemCount) <= firstVisibleItemPosition) {
                loadNext()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // LayoutInflaterは、xmlレイアウトの1つから新しいView（またはLayout）オブジェクトを作成する
        // 動的にxmlレイアウトをセットできる
        adapter = TootListAdapter(layoutInflater, tootList)
        layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        val bindingData: FragmentTootListBinding? = DataBindingUtil.bind(view)
        binding = bindingData ?: return

        bindingData.recyclerView.also {
            it.layoutManager = layoutManager
            it.adapter = adapter
            it.addOnScrollListener(loadNextScrollListener)
        }
        // refreshしたときの動作
        bindingData.swipeRefreshLayout.setOnRefreshListener {
            tootList.clear()
            loadNext()
        }

        loadNext()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding?.unbind()
    }

    // スクロールしてTootを読み込む
    private fun loadNext() {
        lifecycleScope.launch {
            // loadingをセット
            isLoading.set(true)
            // 読み込み中のバーを表示
            showProgress()
            // APIからデータを取得
            val tootListResponse = withContext(Dispatchers.IO) {
                api.fetchPublicTimeline(
                    maxId = tootList.lastOrNull()?.id,
                    onlyMedia = true
                )
            }
            Log.d(TAG, "fetchPublicTimeline")
            // Listに追加
            tootList.addAll(tootListResponse.filter { !it.sensitive })
            Log.d(TAG, "addAll")
            // obserrに通知する
            reloadTootList()
            Log.d(TAG, "reloadTootList")
            // 読み込みを終了
            isLoading.set(false)
            // 次の取得データが存在するかセット
            hasNext.set(tootListResponse.isNotEmpty())
            dismissProgress()
            Log.d(TAG, "dismissProgress")
        }
    }

    private suspend fun showProgress() = withContext(Dispatchers.Main) {
        binding?.swipeRefreshLayout?.isRefreshing = true
    }

    private suspend fun dismissProgress() = withContext(Dispatchers.Main) {
        binding?.swipeRefreshLayout?.isRefreshing = false
    }

    private suspend fun reloadTootList() = withContext(Dispatchers.Main) {
        // DataSetが変更されたことを通知する
        adapter.notifyDataSetChanged()
    }
}
