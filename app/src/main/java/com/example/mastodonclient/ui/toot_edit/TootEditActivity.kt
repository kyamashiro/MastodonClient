package com.example.mastodonclient.ui.toot_edit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mastodonclient.R

class TootEditActivity : AppCompatActivity(), TootEditFragment.Callback {

    companion object {
        val TAG = TootEditActivity::class.java.simpleName

        fun newIntent(context: Context): Intent {
            return Intent(context, TootEditActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toot_edit)

        if (savedInstanceState == null) {
            val fragment = TootEditFragment.newInstance()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment, TootEditFragment.TAG)
                .commit()
        }
    }

    // 投稿完了時にActivityを破棄する
    override fun onPostComplete() {
        setResult(Activity.RESULT_OK)
        finish()
    }
}
