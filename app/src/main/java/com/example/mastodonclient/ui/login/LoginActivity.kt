package com.example.mastodonclient.ui.login

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mastodonclient.R

class LoginActivity : AppCompatActivity(R.layout.activity_login),
    LoginFragment.Callback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            val fragment = LoginFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment, LoginFragment.TAG)
                .commit()
        }
    }

    // ログイン後に画面を終了させる
    override fun onAuthCompleted() {
        Toast.makeText(this, "ログイン完了しました", Toast.LENGTH_LONG).show()
        setResult(Activity.RESULT_OK)
        finish()
    }
}
