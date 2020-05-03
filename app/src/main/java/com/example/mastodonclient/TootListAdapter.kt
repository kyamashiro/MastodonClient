package com.example.mastodonclient

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.mastodonclient.databinding.ListItemTootBinding

class TootListAdapter(private val layoutInflater: LayoutInflater, private val tootList: ArrayList<Toot>) :
    RecyclerView.Adapter<TootListAdapter.ViewHolder>() {

    override fun getItemCount() = tootList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<ListItemTootBinding>(
            layoutInflater,
            R.layout.list_item_toot,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // layout xmlのdataにbindする
        holder.bind(tootList[position])
    }

    class ViewHolder(private val binding: ListItemTootBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(toot: Toot) {
            binding.toot = toot
        }
    }
}
