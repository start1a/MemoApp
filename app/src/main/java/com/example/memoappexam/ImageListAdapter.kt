package com.example.memoappexam

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.memoappexam.data.ImageResource
import com.example.memoappexam.data.MemoImageFilePath
import kotlinx.android.synthetic.main.item_image_memo.view.*

class ImageListAdapter(private val list: MutableList<MemoImageFilePath>) :
    RecyclerView.Adapter<MemoImageViewHolder>() {

    var editable: Boolean = false
    var deleteImageList: MutableList<Int> = mutableListOf()
    lateinit var itemClickListener: (image: String) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoImageViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_image_memo, parent, false)

        // 이미지 아이템 클릭
        view.setOnClickListener {
            val imageResource = it.tag as ImageResource
            // 이미지 삭제 모드
            if (editable) {
                val index = imageResource.selectedIndex
                // 삭제 리스트에 해당 데이터가 선택되어 있으면 제거, 없으면 추가
                if (deleteImageList.contains(index)) {
                    view.deleteCheck.isChecked = false
                    deleteImageList.remove(index)
                } else {
                    view.deleteCheck.isChecked = true
                    deleteImageList.add(index)
                }
            }
            // 이미지 보기 모드
            else itemClickListener(imageResource.imagePath)
        }
        return MemoImageViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    override fun onBindViewHolder(holder: MemoImageViewHolder, position: Int) {
        Glide.with(holder.containerView)
            .load(list[position].run {
                if (this.thumbnailPath.isNotEmpty())
                    this.thumbnailPath
                else this.uri
            })
            .error(R.drawable.icon_no_image_2)
            .override(400)
            .into(holder.containerView.imageItem)

        val imageInfo = ImageResource(list[position].uri, position)
        holder.containerView.tag = imageInfo

        holder.containerView.deleteCheck.isChecked =
            deleteImageList.contains(imageInfo.selectedIndex)
        if (editable) holder.containerView.deleteCheck.visibility = View.VISIBLE
        else holder.containerView.deleteCheck.visibility = View.GONE
    }
}