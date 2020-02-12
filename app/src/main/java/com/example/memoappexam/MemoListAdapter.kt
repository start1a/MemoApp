package com.example.memoappexam

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.memoappexam.data.MemoData
import kotlinx.android.synthetic.main.item_memo.view.*
import java.io.IOException
import java.io.InputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class MemoListAdapter(private val list: MutableList<MemoData>) :
    RecyclerView.Adapter<MemoListViewHolder>() {

    lateinit var itemClickListener: (id: String) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_memo, parent, false)

        view.setOnClickListener {
            itemClickListener.run {
                val id = it.tag as String
                this(id)
            }
        }

        return MemoListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    override fun onBindViewHolder(holder: MemoListViewHolder, position: Int) {
        // 이미지 로딩 실패 시 ic_launcher_backgound 출력
        Glide.with(holder.containerView.imageMemo.context)
            .load(list[position].images[0])
            .error(Glide.with(holder.containerView.imageMemo.context)
            .load(R.drawable.ic_launcher_background))
            .into(holder.containerView.imageMemo)

        // 텍스트
        holder.containerView.textTitle.text = list[position].title
        holder.containerView.textSummary.text = list[position].summary
        holder.containerView.tag = list[position].id
    }
}