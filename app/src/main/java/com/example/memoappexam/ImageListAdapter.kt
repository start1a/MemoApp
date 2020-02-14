package com.example.memoappexam

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_image_memo.view.*

class ImageListAdapter(private val list : MutableList<String>)
    : RecyclerView.Adapter<MemoViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image_memo, parent, false)
        return MemoViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    override fun onBindViewHolder(holder: MemoViewHolder, position: Int) {
        Glide.with(holder.containerView)
            .load(list[position])
            .error(Glide.with(holder.containerView).load(R.drawable.ic_launcher_background))
            .into(holder.containerView.imageItem)
    }
}