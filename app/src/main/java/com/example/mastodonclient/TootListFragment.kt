package com.example.mastodonclient

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mastodonclient.databinding.FragmentTootListBinding

class TootListFragment : Fragment(R.layout.fragment_toot_list) {
    // singleton API
    companion object {
        val TAG = TootListFragment::class.java.simpleName
        private const val API_BASE_URL = "https://androidbook2020.keiji.io"
    }

    private var binding: FragmentTootListBinding? = null

    // 遅延初期化
    private lateinit var adapter: TootListAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private val viewModel: TootListViewModel by viewModels {
        TootListViewModelFactory(
            BuildConfig.INSTANCE_URL,
            BuildConfig.USERNAME,
            lifecycleScope,
            requireContext()
        )
    }

    // Scrollでの読み込み動作
    private val loadNextScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            // loading中, 次のデータがない場合は読み込みしない
            val isLoadingSnapshot = viewModel.isLoading.value ?: return
            if (isLoadingSnapshot || !viewModel.hasNext) {
                return
            }

            val visibleItemCount = recyclerView.childCount
            val totalItemCount = layoutManager.itemCount
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

            if ((totalItemCount - visibleItemCount) <= firstVisibleItemPosition) {
                viewModel.loadNext()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 　tootListの値をArrayListとして代入する
        val tootListSnapshot = viewModel.tootList.value ?: ArrayList<Toot>().also {
            viewModel.tootList.value = it
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
            viewModel.clear()
            viewModel.loadNext()
        }
        // isLoadingの値を監視しプログレスバーの表示を制御する
        viewModel.isLoading.observe(viewLifecycleOwner, Observer {
            binding?.swipeRefreshLayout?.isRefreshing = it
        })
        viewModel.accountInfo.observe(viewLifecycleOwner, Observer {
            showAccountInfo(it)
        })
        // LiveDataの監視 フラグメントに反映させる
        viewModel.tootList.observe(viewLifecycleOwner, Observer {
            adapter.notifyDataSetChanged()
        })
        viewLifecycleOwner.lifecycle.addObserver(viewModel)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding?.unbind()
    }

    // アクションバーにユーザ名をセットする
    private fun showAccountInfo(accountInfo: Account) {
        val activity = requireActivity()
        if (activity is AppCompatActivity) {
            activity.supportActionBar?.subtitle = accountInfo.username
        }
    }
}
