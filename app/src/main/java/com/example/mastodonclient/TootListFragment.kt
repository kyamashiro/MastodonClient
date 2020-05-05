package com.example.mastodonclient

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
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
    private val tootList = MutableLiveData<ArrayList<Toot>>()

    // ようわからんけどLiveDataは監視されていて値が監視されているらしい
    private var isLoading = MutableLiveData<Boolean>()

    // スレッドセーフなboolean
    private var hasNext = AtomicBoolean().apply { set(true) }

    // Scrollでの読み込み動作
    private val loadNextScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            // loading中, 次のデータがない場合は読み込みしない
            val isLoadingSnapshot = isLoading.value ?: return
            if (isLoadingSnapshot || !hasNext.get()) {
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
        // 　tootListの値をArrayListとして代入する
        val tootListSnapshot = tootList.value ?: ArrayList<Toot>().also {
            tootList.value = it
        }
        // LayoutInflaterは、xmlレイアウトの1つから新しいView（またはLayout）オブジェクトを作成する
        // 動的にxmlレイアウトをセットできる
        adapter = TootListAdapter(layoutInflater, tootListSnapshot)
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
            tootListSnapshot.clear()
            loadNext()
        }
        // isLoadingの値を監視しプログレスバーの表示を制御する
        isLoading.observe(viewLifecycleOwner, Observer {
            binding?.swipeRefreshLayout?.isRefreshing = it
        })
        // LiveDataの監視 フラグメントに反映させる
        tootList.observe(viewLifecycleOwner, Observer {
            adapter.notifyDataSetChanged()
        })
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
            isLoading.postValue(true)
            // tootListを取得済みの場合はlaunchのスコープから抜ける?
            val tootListSnapshot = tootList.value ?: return@launch
            // APIからデータを取得
            val tootListResponse = withContext(Dispatchers.IO) {
                api.fetchPublicTimeline(
                    maxId = tootListSnapshot.lastOrNull()?.id,
                    onlyMedia = true
                )
            }
            Log.d(TAG, "fetchPublicTimeline")
            // Listに追加
            tootListSnapshot.addAll(tootListResponse.filter { !it.sensitive })
            Log.d(TAG, "addAll")
            // obserrに通知する
            reloadTootList()
            Log.d(TAG, "reloadTootList")
            // 読み込みを終了
            isLoading.postValue(false)
            // 次の取得データが存在するかセット
            hasNext.set(tootListResponse.isNotEmpty())
            Log.d(TAG, "dismissProgress")
        }
    }

    private suspend fun reloadTootList() = withContext(Dispatchers.Main) {
        // DataSetが変更されたことを通知する
        adapter.notifyDataSetChanged()
    }
}
