package com.example.memoappexam

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.memoappexam.data.MemoImageData
import kotlinx.android.synthetic.main.item_image_memo.view.*
import java.net.URL

class ImageListAdapter(private val list : MutableList<MemoImageData>)
    : RecyclerView.Adapter<MemoImageViewHolder>() {

    lateinit var itemClickListener: (image: String) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image_memo, parent, false)
        view.setOnClickListener {
            itemClickListener.run {
                val id = it.tag as String
                this(id)
            }
        }
        return MemoImageViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    override fun onBindViewHolder(holder: MemoImageViewHolder, position: Int) {
        Glide.with(holder.containerView)
            .load(list[position].image)
            .into(holder.containerView.imageItem)

        holder.containerView.tag = list[position].image
    }
}