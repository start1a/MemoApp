package com.start3a.memoji.views.MemoList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.start3a.memoji.R
import com.start3a.memoji.data.MemoData
import com.start3a.memoji.ListViewHolder
import kotlinx.android.synthetic.main.item_memo.view.*
import java.text.SimpleDateFormat
import java.util.*

class MemoListAdapter(private val list: MutableList<MemoData>, val layoutId: Int) :
    RecyclerView.Adapter<ListViewHolder>() {

    lateinit var itemClickListener: (id: String) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)

        view.setOnClickListener {
            itemClickListener.run {
                val id = it.tag as String
                this(id)
            }
        }

        return ListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.containerView.run {

            // 이미지가 없으면 뷰를 제거
            if (list[position].imageFileLinks.size > 0) {
                imageMemo.visibility = View.VISIBLE
                // 썸네일 이미지
                val image = list[position].imageFileLinks[0]!!

                Glide.with(this)
                    .load(image.uri)
                    .error(AlternativeThumbnailImage(this, image.thumbnailPath))
                    .override(400)
                    .into(imageMemo)
            } else
                imageMemo.visibility = View.GONE

            // 알람 존재 여부
            if (list[position].alarmTimeList.size > 0)
                alarm_exist.visibility = View.VISIBLE
            else
                alarm_exist.visibility = View.GONE

            // 텍스트
            // 제목
            if (list[position].title.isNotEmpty()) {
                textTitle.visibility = View.VISIBLE

                if (list[position].title.length > 25) {
                    textTitle.text = list[position].title.substring(0..25)
                    textTitle.append("..")
                } else textTitle.text = list[position].title
            } else textTitle.visibility = View.GONE
            // 내용
            if (list[position].content.isNotEmpty()) {
                textSummary.visibility = View.VISIBLE
                textSummary.text = list[position].summary
            } else textSummary.visibility = View.GONE
            // 최근 수정 날짜
            textDate.text = GetDateFormat(list[position].date)
            // 태그
            // intent 전송
            tag = list[position].id
        }
    }

    private fun AlternativeThumbnailImage(view: View, imagePath: String) =
        Glide.with(view)
            .load(imagePath)
            .error(R.drawable.icon_error)

    private fun GetDateFormat(date: Date): String {
        return SimpleDateFormat("yy.MM.dd HH:mm").format(date)
    }
}