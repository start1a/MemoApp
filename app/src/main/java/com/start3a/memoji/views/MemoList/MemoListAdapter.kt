package com.start3a.memoji.views.MemoList

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.start3a.memoji.ImageTransform
import com.start3a.memoji.R
import com.start3a.memoji.data.MemoData
import kotlinx.android.synthetic.main.item_memo.view.*
import java.util.*

class MemoListAdapter(private val list: MutableList<MemoData>, val layoutId: Int) :
    RecyclerView.Adapter<MemoViewHolder>() {

    lateinit var itemClickListener: (id: String) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)

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
        // 이미지가 없으면 뷰를 제거
        if (list[position].imageFileLinks.size > 0) {
            holder.containerView.imageMemo.visibility = View.VISIBLE
            // 썸네일 이미지
            val image = list[position].imageFileLinks[0]!!
            Glide.with(holder.containerView)
                .load(image.uri)
                .error(AlternativeThumbnailImage(holder, image.thumbnailPath))
                .override(400)
                .into(holder.containerView.imageMemo)
        } else holder.containerView.imageMemo.visibility = View.GONE

        // 알람 존재 여부
        if (list[position].alarmTimeList.size > 0)
            holder.containerView.alarm_exist.visibility = View.VISIBLE
        else
            holder.containerView.alarm_exist.visibility = View.GONE

        // 텍스트
        // 제목
        if (list[position].title.isNotEmpty()) {
            holder.containerView.textTitle.visibility = View.VISIBLE

            if (list[position].title.length > 20)
                holder.containerView.textTitle.text = list[position].title.substring(0..25) + ".."
            else holder.containerView.textTitle.text = list[position].title
        } else holder.containerView.textTitle.visibility = View.GONE
        // 내용
        if (list[position].content.isNotEmpty()) {
            holder.containerView.textSummary.visibility = View.VISIBLE
            holder.containerView.textSummary.text = list[position].summary
        } else holder.containerView.textSummary.visibility = View.GONE
        // 최근 수정 날짜
        holder.containerView.textDate.text = GetDateFormat(list[position].date)
        // 태그
        // intent 전송
        holder.containerView.tag = list[position].id
    }

    private fun AlternativeThumbnailImage(holder: MemoViewHolder, imagePath: String) =
        Glide.with(holder.containerView)
            .load(imagePath.run {
                val prevBitmap = BitmapFactory.decodeFile(imagePath)
                ImageTransform.getRotatedBitmap(
                    prevBitmap,
                    ImageTransform.getOrientationOfImage(
                        imagePath
                    )
                )
            })
            .error(R.drawable.icon_error)

    private fun GetDateFormat(date: Date): String {
        return java.text.SimpleDateFormat("yy.MM.dd HH:mm").format(date)
    }
}