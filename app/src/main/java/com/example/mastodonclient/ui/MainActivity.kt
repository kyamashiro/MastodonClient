package com.example.mastodonclient.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mastodonclient.R
import com.example.mastodonclient.ui.toot_list.TootListFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // 画面が再生成されたときのことを考慮して画面がはじめて作成された時ににだけFragmentを追加するようにする
        if (savedInstanceState == null) {
            val fragment = TootListFragment()
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
