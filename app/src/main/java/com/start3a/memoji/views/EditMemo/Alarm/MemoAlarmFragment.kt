package com.start3a.memoji.views.EditMemo.Alarm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.start3a.memoji.R
import com.start3a.memoji.viewmodel.EditMemoViewModel
import kotlinx.android.synthetic.main.fragment_memo_alarm.*

/**
 * A simple [Fragment] subclass.
 */
class MemoAlarmFragment : Fragment() {

    private var viewModel: EditMemoViewModel? = null
    private lateinit var listAlarmAdapter: AlarmListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_memo_alarm, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = activity!!.application!!.let {
            ViewModelProvider(
                activity!!.viewModelStore,
                ViewModelProvider.AndroidViewModelFactory(it)
            )
                .get(EditMemoViewModel::class.java)
        }

        viewModel!!.let { VM ->
            // 리스트 초기화
            listAlarmAdapter = AlarmListAdapter(VM.alarmTimeList.value!!).also {
                alarmListView.adapter = it
                alarmListView.layoutManager = LinearLayoutManager(activity)
                it.deleteAlarmList = viewModel!!.deleteDataList
                it.itemClickListener = { index ->
                    VM.datePickerShowListener(index)
                }
            }

            // 리스트 갱신
            VM.alarmTimeList.observe(viewLifecycleOwner, Observer {
                listAlarmAdapter.notifyDataSetChanged()
                // 추가 버튼
                if (it.size > 0) textInformNoAlarm.visibility = View.GONE
                else textInformNoAlarm.visibility = View.VISIBLE
            })

            // 수정 모드
            VM.editable.observe(viewLifecycleOwner, Observer { editable ->
                listAlarmAdapter.let {
                    // 알람 내용 수정 모드
                    if (!editable) it.deleteAlarmList.clear()
                    it.editable = editable
                    // 알람 삭제 라디오 버튼 체크 visible / gone
                    it.notifyDataSetChanged()
                }
            })

            // 알람 삭제 대상 모두 체크
            VM.allContentSelectListener = {
                val list = listAlarmAdapter.deleteAlarmList
                val sizeAlarmList = VM.alarmTimeList.value!!.size
                // 내림차순 정렬에 유리하도록 거꾸로 추가
                for (i in sizeAlarmList - 1 downTo 0) {
                    if (!list.contains(i))
                        list.add(i)
                }
                listAlarmAdapter.notifyDataSetChanged()
            }

            // 선택된 알람 삭제 리스너
            VM.deleteAlarmListListener = {
                // 데이터 삭제
                VM.DeleteAlarm(listAlarmAdapter.deleteAlarmList)
                // 삭제 후 선택 아이템 체크 삭제
                listAlarmAdapter.let {
                    it.deleteAlarmList.clear()
                    it.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 삭제 리스트 유지
        viewModel!!.deleteDataList = listAlarmAdapter.deleteAlarmList
    }

}
