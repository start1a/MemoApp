package com.example.memoappexam.views


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.memoappexam.ImageListAdapter
import com.example.memoappexam.R
import com.example.memoappexam.viewmodel.DetailViewModel
import kotlinx.android.synthetic.main.fragment_memo_image_list.*
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class MemoImageFragment : Fragment() {

    private lateinit var listImageAdapter: ImageListAdapter
    private var viewModel: DetailViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_memo_image_list, container, false)
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
            it.image.observe(this, Observer { listImageAdapter.notifyDataSetChanged() })
            // T: 수정모드, F: 보기모드
            it.editMode.observe(this, Observer {
                // 이미지 자세히 보기
                if (!it) {
                    listImageAdapter.let {
                        it.itemClickListener = {
                            val intent = Intent(activity, ImageViewActivity::class.java)
                            intent.putExtra("image", it)
                            startActivity(intent)
                        }
                        it.deleteImageList.clear()
                    }
                }
                // 삭제 모드 여부
                listImageAdapter.editMode = it
                listImageAdapter.notifyDataSetChanged()
            })
            it.deleteImageListListener = {
                val list = listImageAdapter.deleteImageList
                // 내림차순 정렬 후 인덱스 별 삭제
                Collections.sort(list, Collections.reverseOrder())
                for (i in 0..list.size - 1) {
                    it.image.value?.removeAt(list[i])
                }

                listImageAdapter.let {
                    it.deleteImageList.clear()
                    it.notifyDataSetChanged()
                }
            }

            // 이미지 리스트
            it.image.value?.let {
                listImageAdapter = ImageListAdapter(it)
                listImageAdapter.deleteImageList = viewModel!!.deleteImageList
                imgListView.layoutManager = GridLayoutManager(activity, 3)
                imgListView.adapter = listImageAdapter
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel!!.saveDeleteImageList(listImageAdapter.deleteImageList)
    }
}