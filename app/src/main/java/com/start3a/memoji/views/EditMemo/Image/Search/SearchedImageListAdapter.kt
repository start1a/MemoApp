package com.start3a.memoji.views.EditMemo.Image.Search

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.start3a.memoji.ListViewHolder
import com.start3a.memoji.R
import com.start3a.memoji.data.NaverImage
import kotlinx.android.synthetic.main.item_image_searched.view.*

// 이미지 검색 API로부터 받은 리스트의 어댑터
class SearchedImageListAdapter(private val list: MutableList<NaverImage>) :
    RecyclerView.Adapter<ListViewHolder>() {

    lateinit var selectionList: MutableList<NaverImage>
    lateinit var itemClickListener: (list: MutableList<NaverImage>, index: Int) -> Unit


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_image_searched, parent, false)
        return ListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.containerView.run {
            Glide.with(this)
                .load(list[position].thumbnail)
                .override(400)
                .error(
                    Glide.with(this)
                        .load(R.drawable.icon_error)
                )
                .into(imageItem)

            imageItem.setOnClickListener {
                if (selectionList.contains(list[position])) {
                    textNum.visibility = View.GONE
                    selectionList.remove(list[position])
                } else {
                    textNum.visibility = View.VISIBLE
                    selectionList.add(list[position])
                }
                Log.d("TAG", selectionList.toString())
            }

            imgBtnDetail.setOnClickListener {
                itemClickListener(list, position)
            }

            tag = position
        }
    }
}