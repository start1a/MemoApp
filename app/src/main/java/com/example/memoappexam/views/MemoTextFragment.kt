package com.example.memoappexam.views


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

import com.example.memoappexam.R
import com.example.memoappexam.viewmodel.DetailViewModel
import kotlinx.android.synthetic.main.fragment_memo_text.*

/**
 * A simple [Fragment] subclass.
 */
class MemoTextFragment : Fragment() {

    private var viewModel: DetailViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_memo_text, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = activity!!.application!!.let {
            ViewModelProvider(
                activity!!.viewModelStore,
                ViewModelProvider.AndroidViewModelFactory(it)
            )
                .get(DetailViewModel::class.java)
        }

        viewModel!!.let {
            // 메모 갱신
            it.title.observe(this, Observer { editTitle.setText(it) })
            it.content.observe(this, Observer { editContent.setText(it) })
            // T: 수정모드, F: 보기모드
            it.editMode.observe(this, Observer {
                editTitle.isFocusable = it
                editTitle.isFocusableInTouchMode = it
                editContent.isFocusable = it
                editContent.isFocusableInTouchMode = it
            })
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel!!.saveLiveData(editTitle.text.toString(), editContent.text.toString())
    }
}
