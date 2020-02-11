package com.example.memoappexam

import android.text.Layout
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.memoappexam.data.MemoData
import kotlinx.android.synthetic.main.item_memo.view.*

class MemoListAdapter(val list: MutableList<MemoData>) : RecyclerView.Adapter<MemoListViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_memo, parent, false)
        return MemoListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    override fun onBindViewHolder(holder: MemoListViewHolder, position: Int) {
        if (list[position].images.count() > 0)
            holder.containerView.imageMemo.setImageBitmap(list[position].images[0])
    }
}