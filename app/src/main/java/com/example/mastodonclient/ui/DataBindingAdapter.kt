package com.example.mastodonclient.ui

import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.example.mastodonclient.entity.Media

// 既存のクラスを拡張しているっぽい
// https://jumperson.hatenablog.com/entry/2016/06/07/135256
// https://qiita.com/pinemz/items/640428706dac1bfae409#kotlin-%E3%81%A7%E3%81%AE%E6%8B%A1%E5%BC%B5%E9%96%A2%E6%95%B0%E3%81%A8%E3%81%9D%E3%81%AE%E5%AE%9F%E8%A3%85
@BindingAdapter("spannedContent")
fun TextView.setSpannedString(content: String) {
    text = HtmlCompat.fromHtml(
        content,
        HtmlCompat.FROM_HTML_MODE_COMPACT
    )
}

@BindingAdapter("media")
fun ImageView.setMedia(media: Media?) {
    if (media == null) {
        setImageDrawable(null)
        return
    }
    Glide.with(this)
        .load(media.url)
        .into(this)
}
