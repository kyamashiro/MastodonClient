package com.example.mastodonclient.ui.toot_list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.mastodonclient.R
import com.example.mastodonclient.databinding.ListItemTootBinding
import com.example.mastodonclient.entity.Toot

class TootListAdapter(
    private val layoutInflater: LayoutInflater,
    private val tootList: ArrayList<Toot>,
    private val callback: Callback?
) :
    RecyclerView.Adapter<TootListAdapter.ViewHolder>() {

    interface Callback {
        fun openDetail(toot: Toot)
        fun delete(toot: Toot)
    }

    override fun getItemCount() = tootList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<ListItemTootBinding>(
            layoutInflater,
            R.layout.list_item_toot,
            parent,
            false
        )
        return ViewHolder(binding, callback)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // layout xmlのdataにbindする
        holder.bind(tootList[position])
    }

    class ViewHolder(
        private val binding: ListItemTootBinding,
        private val callback: Callback?
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(toot: Toot) {
            binding.toot = toot
            binding.root.setOnClickListener {
                callback?.openDetail(toot)
            }
            binding.more.setOnClickListener {
                PopupMenu(itemView.context, it).also { popupMenu ->
                    popupMenu.menuInflater.inflate(
                        R.menu.toot_detail,
                        popupMenu.menu
                    )
                    popupMenu.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.menu_delete -> callback?.delete(toot)
                        }
                        return@setOnMenuItemClickListener true
                    }
                }.show()
            }
        }
    }
}
