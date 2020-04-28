package com.example.memoappexam

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.memoappexam.data.MemoData
import kotlinx.android.synthetic.main.item_memo.view.*

class MemoListAdapter(private val list: MutableList<MemoData>) :
    RecyclerView.Adapter<MemoViewHolder>() {

    lateinit var itemClickListener: (id: String) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_memo, parent, false)

        view.setOnClickListener {
            itemClickListener.run {
                val id = it.tag as String
                this(id)
            }
        }

        return MemoViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    override fun onBindViewHolder(holder: MemoViewHolder, position: Int) {
        // 썸네일 이미지
        if (list[position].imageFileLinks.size > 0)
        {
            Glide.with(holder.containerView)
                .load(list[position].imageFileLinks[0]?.run {
                    if (this.thumbnailPath.isNotEmpty())
                        this.thumbnailPath
                    else
                        this.uri
                })
                .error(R.drawable.icon_error)
                .into(holder.containerView.imageMemo)
        }
        else
        {
            Glide.with(holder.containerView)
                .load(R.drawable.icon_no_image_1)
                .into(holder.containerView.imageMemo)
        }

        // 텍스트
        if (list[position].title.length > 20)
            holder.containerView.textTitle.text = list[position].title.substring(0..25) + ".."
        else holder.containerView.textTitle.text = list[position].title
        holder.containerView.textSummary.text = list[position].summary
        holder.containerView.tag = list[position].id
    }
}