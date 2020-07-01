package com.start3a.memoji.views.EditMemo.Image

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.start3a.memoji.ListViewHolder
import com.start3a.memoji.R
import kotlinx.android.synthetic.main.item_image_detail.view.*

// 메모의 이미지 리스트 어댑터
class ImageDetailViewListAdapter(
    private val list: MutableList<String>,
    private val listAlternative: MutableList<String>
) :
    RecyclerView.Adapter<ListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_image_detail, parent, false)
        return ListViewHolder(view)

    }

    override fun getItemCount(): Int {
        return list.count()
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.containerView.run {
            Glide.with(this)
                .load(list[position])
                .error(AlternativeImage(this, position.let {
                    if (listAlternative.size > it)
                        listAlternative[position]
                    else ""
                }))
                .into(photo_view)
        }
    }

    private fun AlternativeImage(view: View, imagePath: String) =
        Glide.with(view)
            .load(imagePath)
            .error(R.drawable.icon_error)
}