package com.example.mastodonclient.ui.toot_list

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mastodonclient.BuildConfig
import com.example.mastodonclient.R
import com.example.mastodonclient.databinding.FragmentTootListBinding
import com.example.mastodonclient.entity.Account
import com.example.mastodonclient.entity.Toot
import com.example.mastodonclient.ui.login.LoginActivity
import com.example.mastodonclient.ui.toot_detail.TootDetailActivity
import com.example.mastodonclient.ui.toot_edit.TootEditActivity
import com.google.android.material.snackbar.Snackbar

class TootListFragment : Fragment(R.layout.fragment_toot_list),
    TootListAdapter.Callback {
    // singleton API
    companion object {
        val TAG = TootListFragment::class.java.simpleName

        private const val BUNDLE_KEY_TIMELINE_TYPE_ORDINAL = "timeline_type_ordinal"
        private const val REQUEST_CODE_TOOT_EDIT = 0x01
        private const val REQUEST_CODE_LOGIN = 0x02

        @JvmStatic
        fun newInstance(timelineType: TimelineType): TootListFragment {
            val args = Bundle().apply {
                putInt(BUNDLE_KEY_TIMELINE_TYPE_ORDINAL, timelineType.ordinal)
            }
            return TootListFragment().apply {
                arguments = args
            }
        }
    }

    private var binding: FragmentTootListBinding? = null

    // 遅延初期化
    private lateinit var adapter: TootListAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private var timelineType = TimelineType.PublicTimeline

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireArguments().also {
            val typeOrdinal = it.getInt(
                BUNDLE_KEY_TIMELINE_TYPE_ORDINAL,
                TimelineType.PublicTimeline.ordinal
            )
            timelineType = TimelineType.values()[typeOrdinal]
        }
    }

    private val viewModel: TootListViewModel by viewModels {
        TootListViewModelFactory(
            BuildConfig.INSTANCE_URL,
            BuildConfig.USERNAME,
            timelineType,
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
        adapter = TootListAdapter(layoutInflater, lifecycleScope, this)
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

        // r.id.fab 投稿ボタンをタップしたときに投稿画面に遷移する
        bindingData.fab.setOnClickListener {
            launchTootEditActivity()
        }
        viewModel.loginRequired.observe(viewLifecycleOwner, Observer {
            if (it) {
                launchLoginActivity()
            }
        })
        // isLoadingの値を監視しプログレスバーの表示を制御する
        viewModel.isLoading.observe(viewLifecycleOwner, Observer {
            binding?.swipeRefreshLayout?.isRefreshing = it
        })
        // エラーメッセージの表示
        viewModel.errorMessage.observe(viewLifecycleOwner, Observer {
            Snackbar.make(bindingData.swipeRefreshLayout, it, Snackbar.LENGTH_LONG).show()
        })
        // アカウント名をアクションバーにセットする
        viewModel.accountInfo.observe(viewLifecycleOwner, Observer {
            showAccountInfo(it)
        })
        // LiveDataの監視 フラグメントに反映させる
        viewModel.tootList.observe(viewLifecycleOwner, Observer {
            adapter.tootList = it
        })
        viewLifecycleOwner.lifecycle.addObserver(viewModel)
    }

    private fun handleLoginActivityResult(resultCode: Int) {
        when (resultCode) {
            Activity.RESULT_OK -> viewModel.reloadUserCredential()
            else -> {
                Toast.makeText(
                    requireContext(),
                    "ログインが完了しませんでした",
                    Toast.LENGTH_LONG
                ).show()
                requireActivity().finish()
            }
        }
    }

    override fun openDetail(toot: Toot) {
        val intent = TootDetailActivity.newIntent(requireContext(), toot)
        startActivity(intent)
    }

    // アクティビティから結果を受け取る
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 投稿完了時にタイムラインを再読込する
        if (requestCode == REQUEST_CODE_TOOT_EDIT &&
            resultCode == Activity.RESULT_OK
        ) {
            viewModel.clear()
            viewModel.loadNext()
        }
        // 未ログイン時にログイン画面に遷移する
        if (requestCode == REQUEST_CODE_LOGIN) {
            handleLoginActivityResult(resultCode)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding?.unbind()
    }

    private fun launchTootEditActivity() {
        val intent = TootEditActivity.newIntent(requireContext())
        startActivityForResult(intent, REQUEST_CODE_TOOT_EDIT)
    }

    // アクションバーにユーザ名をセットする
    private fun showAccountInfo(accountInfo: Account) {
        val activity = requireActivity()
        if (activity is AppCompatActivity) {
            activity.supportActionBar?.subtitle = accountInfo.username
        }
    }

    private fun launchLoginActivity() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_LOGIN)
    }

    override fun delete(toot: Toot) {
        viewModel.delete(toot)
    }
}
