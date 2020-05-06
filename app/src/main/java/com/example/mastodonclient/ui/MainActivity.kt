package com.example.mastodonclient.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.mastodonclient.R
import com.example.mastodonclient.databinding.ActivityMainBinding
import com.example.mastodonclient.ui.toot_list.TimelineType
import com.example.mastodonclient.ui.toot_list.TootListFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)
        // bottomナビゲーションUI
        // ナビゲーションの選択に応じてUIを切り替える
        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            // it.itemIdはbottom_navigationのid_name
            val fragment = when (it.itemId) {
                R.id.menu_home -> {
                    TootListFragment.newInstance(TimelineType.HomeTimeline)
                }
                R.id.menu_public -> {
                    TootListFragment.newInstance(TimelineType.PublicTimeline)
                }
                else -> null
            }
            fragment ?: return@setOnNavigationItemSelectedListener false

            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    fragment,
                    TootListFragment.TAG
                )
                .commit()

            return@setOnNavigationItemSelectedListener true
        }

        // 画面が再生成されたときのことを考慮して画面がはじめて作成された時ににだけFragmentを追加するようにする
        if (savedInstanceState == null) {
            val fragment = TootListFragment.newInstance(
                TimelineType.HomeTimeline
            )
            supportFragmentManager.beginTransaction()
                .add(
                    R.id.fragment_container,
                    fragment,
                    TootListFragment.TAG
                )
                .commit()
        }
    }
}
