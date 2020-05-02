package com.start3a.memoji.views


import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.start3a.memoji.R
import com.start3a.memoji.viewmodel.DetailViewModel
import kotlinx.android.synthetic.main.fragment_memo_text.*

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

        editTitle.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel!!.memoTitleSaveListener()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        editContent.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel!!.memoContentSaveListener()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        viewModel!!.let {
            it.title.observe(this, Observer { editTitle.setText(it) })
            it.content.observe(this, Observer { editContent.setText(it) })
            // T: 수정모드, F: 보기모드
            it.editable.observe(this, Observer { mode ->
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
            it.memoTitleSaveListener = { it.titleTemp = editTitle.text.toString() }
            it.memoContentSaveListener = { it.contentTemp = editContent.text.toString() }
            // 기존의 메모 로드
            it.saveText()
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
