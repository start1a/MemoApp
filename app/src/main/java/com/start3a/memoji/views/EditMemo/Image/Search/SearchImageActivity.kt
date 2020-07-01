package com.start3a.memoji.views.EditMemo.Image.Search

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.start3a.memoji.R
import com.start3a.memoji.data.NaverImage
import com.start3a.memoji.viewmodel.SearchImageViewModel
import com.start3a.memoji.views.EditMemo.Image.ImageViewActivity
import kotlinx.android.synthetic.main.activity_search_image.*

class SearchImageActivity : AppCompatActivity() {

    private var viewModel: SearchImageViewModel? = null
    private lateinit var listAdpater: SearchedImageListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_image)

        viewModel = application!!.let { app ->
            ViewModelProvider(viewModelStore, ViewModelProvider.AndroidViewModelFactory(app))
                .get(SearchImageViewModel::class.java)
        }

        viewModel!!.let { VM ->
            VM.imageList.let {
                listAdpater = SearchedImageListAdapter(it.value!!).apply {
                    selectionList = VM.selectionList
                    itemClickListener = { sendDataList, index ->
                        val intent =
                            Intent(this@SearchImageActivity, ImageViewActivity::class.java).apply {
                                putStringArrayListExtra("images", getImagePathList(sendDataList))
                                putExtra("selectedIndex", index)
                            }
                        startActivity(intent)
                    }
                }
                searchedImageListView.adapter = listAdpater
                searchedImageListView.layoutManager = GridLayoutManager(this, 2)

                it.observe(this, Observer {
                    listAdpater.notifyDataSetChanged()
                })
            }
            editSearchImage.setText(VM.searchText)
            editSearchImage.setOnEditorActionListener { v, actionId, event ->
                var handled = false

                if (v.id == R.id.editSearchImage && actionId == EditorInfo.IME_ACTION_SEARCH) {
                    v.hideKeyboard()
                    VM.clearList()
                    VM.getImageData(editSearchImage.text.toString())
                    handled = true
                }
                handled
            }

            // 리스트 스크롤 이벤트 리스너
            // 스크롤을 통해 마지막 아이템에 도착할 경우 다음 데이터 추가
            searchedImageListView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val lastVisibleItemPosition =
                        (recyclerView.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()
                    val itemTotalCount = recyclerView.adapter?.itemCount?.minus(1)

                    if (lastVisibleItemPosition == itemTotalCount) {
                        VM.getImageData(editSearchImage.text.toString())
                    }
                }
            })
        }
    }

    private fun View.hideKeyboard() {
        val imm =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun getImagePathList(inputList: MutableList<NaverImage>): ArrayList<String> {
        return arrayListOf<String>().apply {
            inputList.forEach { add(it.link) }
        }
    }

    override fun onBackPressed() {
        val intent = Intent().apply {
            val list = arrayListOf<String>().apply {
                listAdpater.selectionList.forEach { add(it.link) }
            }
            putStringArrayListExtra("resImages", list)
        }
        setResult(Activity.RESULT_OK, intent)
        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        viewModel!!.searchText = editSearchImage.text.toString()
    }
}
