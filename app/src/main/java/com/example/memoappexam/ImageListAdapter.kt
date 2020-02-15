package com.example.memoappexam

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.memoappexam.data.MemoImageData
import io.realm.RealmList
import kotlinx.android.synthetic.main.item_image_memo.view.*

class ImageListAdapter(private val list : RealmList<MemoImageData>)
    : RecyclerView.Adapter<MemoImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image_memo, parent, false)
        return MemoImageViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    override fun onBindViewHolder(holder: MemoImageViewHolder, position: Int) {
        Glide.with(holder.containerView)
            .load(list[position]?.image)
            .error(Glide.with(holder.containerView).load(R.drawable.ic_launcher_background))
            .into(holder.containerView.imageItem)
    }

    fun getList(): RealmList<MemoImageData> {
        return list
    }
}