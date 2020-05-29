package com.start3a.memoji.views.EditMemo.Text


import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.start3a.memoji.R
import com.start3a.memoji.viewmodel.EditMemoViewModel
import kotlinx.android.synthetic.main.fragment_memo_text.*

class MemoTextFragment : Fragment() {

    private var viewModel: EditMemoViewModel? = null

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
            ViewModelProvider(activity!!.viewModelStore,
                ViewModelProvider.AndroidViewModelFactory(it)
            )
                .get(EditMemoViewModel::class.java)
        }

        editTitle.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel!!.titleTemp = editTitle.text.toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        editContent.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel!!.contentTemp = editContent.text.toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        viewModel!!.let { VM ->
            VM.title.observe(this, Observer { editTitle.setText(it) })
            VM.content.observe(this, Observer { editContent.setText(it) })
            // T: 수정모드, F: 보기모드
            VM.editable.observe(this, Observer { mode ->
                setEditMode(editTitle, mode)
                setEditMode(editContent, mode)

                // 키보드 출력
                if (mode) {
                    if (editTitle.text.isEmpty())
                        showKeyboard(editTitle)
                    else if (editContent.text.isEmpty())
                        showKeyboard(editContent)
                } else {
                    editTitle.hideKeyboard()
                    editContent.hideKeyboard()
                }
            })
            // 컨텐츠 모두 선택
            VM.AllContentSelectListener = {
                AlertDialog.Builder(activity!!)
                    .setTitle("제목과 내용을 모두 삭제하시겠습니까?")
                    .setNegativeButton("취소", null)
                    .setPositiveButton("확인") { _, _ ->
                        VM.titleTemp = ""
                        VM.contentTemp = ""
                        VM.saveText()
                    }.show()
            }
            // 기존의 메모 로드
            VM.saveText()
        }
    }

    private fun showKeyboard(view: EditText) {
        view.requestFocus()
        val imm =
            activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun View.hideKeyboard() {
        val imm =
            activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun setEditMode(view: EditText, editable: Boolean) {
        view.isFocusable = editable
        view.isFocusableInTouchMode = editable
        view.isCursorVisible = editable
    }
}
