package com.example.memoappexam.views


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.memoappexam.ImageListAdapter

import com.example.memoappexam.R
import com.example.memoappexam.viewmodel.DetailViewModel
import kotlinx.android.synthetic.main.content_edit_memo.*
import kotlinx.android.synthetic.main.fragment_memo_image_list.*

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
            it.image.value?.let {
                listImageAdapter = ImageListAdapter(it)
                imgListView.adapter = listImageAdapter
                imgListView.layoutManager = GridLayoutManager(activity, 3)
            }
            it.image.observe(this, Observer { listImageAdapter.notifyDataSetChanged() })
        }
    }

}