package com.start3a.memoji.views.EditMemo.Alarm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.start3a.memoji.R
import com.start3a.memoji.data.ImageResource
import com.start3a.memoji.data.MemoImageFilePath
import kotlinx.android.synthetic.main.item_alarm_memo.view.*
import kotlinx.android.synthetic.main.item_image_memo.view.*
import java.text.SimpleDateFormat
import java.util.*

// 메모의 이미지 리스트 어댑터
class AlarmListAdapter(private val list: MutableList<Date>) :
    RecyclerView.Adapter<MemoAlarmViewHolder>() {

    var editable: Boolean = false
    lateinit var deleteAlarmList: MutableList<Int>
    lateinit var itemClickListener: (index: Int) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoAlarmViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_alarm_memo, parent, false)

        // 알람 아이템 클림
        view.setOnClickListener {
            val index = it?.tag as Int
            // 알람 삭제 모드
            if (editable) {
                // 삭제 리스트에 해당 데이터가 선택되어 있으면 제거, 없으면 추가
                if (deleteAlarmList.contains(index)) {
                    it.deleteRadioButton.isChecked = false
                    deleteAlarmList.remove(index)
                } else {
                    it.deleteRadioButton.isChecked = true
                    deleteAlarmList.add(index)
                }
            }
            // 알람 수정
            else itemClickListener(index)
        }
        return MemoAlarmViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    override fun onBindViewHolder(holder: MemoAlarmViewHolder, position: Int) {
        // 삭제 라디오 버튼
        if (editable) holder.containerView.deleteRadioButton.visibility = View.VISIBLE
        else holder.containerView.deleteRadioButton.visibility = View.GONE
        holder.containerView.deleteRadioButton.isChecked = deleteAlarmList.contains(position)
        // 시간
        holder.containerView.textTime.text = SimpleDateFormat("hh:mm").format(list[position])
        // 날짜
        holder.containerView.textDate.text = SimpleDateFormat("yyyy.MM.dd").format(list[position])
        // on / off (추가 예정)


        // 태그
        holder.containerView.tag = position
    }
}