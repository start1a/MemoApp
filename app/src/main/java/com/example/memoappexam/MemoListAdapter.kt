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
        // 이미지 로딩 실패 시 ic_launcher_backgound 출력
        Glide.with(holder.containerView)
            .load(list[position].images[0])
            .error(Glide.with(holder.containerView.imageMemo.context).load(R.drawable.ic_launcher_background))
            .into(holder.containerView.imageMemo)

        // 텍스트
        holder.containerView.textTitle.text = list[position].title
        holder.containerView.textSummary.text = list[position].summary
        holder.containerView.tag = list[position].id
    }
}