package com.example.mastodonclient

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // リソースのIDがtextviewのオブジェクトを取得する
        val textView: TextView = findViewById(R.id.textview)
        // 取得したオブジェクトのtext変数に代入する
        textView.text = "Hello XML Layout!"
    }
}
