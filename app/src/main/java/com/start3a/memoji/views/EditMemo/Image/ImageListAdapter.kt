package com.start3a.memoji.views.EditMemo.Image

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.start3a.memoji.R
import com.start3a.memoji.data.MemoImageFilePath
import kotlinx.android.synthetic.main.item_image_memo.view.*

// 메모의 이미지 리스트 어댑터
class ImageListAdapter(private val list: MutableList<MemoImageFilePath>) :
    RecyclerView.Adapter<MemoImageViewHolder>() {

    var editable: Boolean = false
    lateinit var deleteImageList: MutableList<Int>
    lateinit var itemClickListener: (list: MutableList<MemoImageFilePath>, index: Int) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoImageViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_image_memo, parent, false)

        // 이미지 아이템 클릭
        view.setOnClickListener {
            val index = it.tag as Int
            // 이미지 삭제 모드
            if (editable) {
                // 삭제 리스트에 해당 데이터가 선택되어 있으면 제거, 없으면 추가
                if (deleteImageList.contains(index)) {
                    view.itemCheck.isChecked = false
                    deleteImageList.remove(index)
                } else {
                    view.itemCheck.isChecked = true
                    deleteImageList.add(index)
                }
            }
            // 이미지 보기 모드
            else itemClickListener(list, index)
        }
        return MemoImageViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    override fun onBindViewHolder(holder: MemoImageViewHolder, position: Int) {
        holder.containerView.run {
            // 이미지
            Glide.with(this)
                .load(list[position].thumbnailPath)
                .error(AlternativeImage(holder, list[position].uri))
                .into(imageItem)

            // 태그
            tag = position

            // 삭제 체크박스
            itemCheck.isChecked = deleteImageList.contains(position)
            if (editable) itemCheck.visibility = View.VISIBLE
            else itemCheck.visibility = View.GONE
        }
    }

    private fun AlternativeImage(holder: MemoImageViewHolder, imagePath: String) =
        Glide.with(holder.containerView)
            .load(imagePath)
            .error(R.drawable.icon_error)
            .override(400)
}