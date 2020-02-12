package com.example.memoappexam.views


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memoappexam.MemoListAdapter
import com.example.memoappexam.MemoListViewHolder

import com.example.memoappexam.R
import com.example.memoappexam.data.MemoData
import com.example.memoappexam.viewmodel.MemoListViewModel
import kotlinx.android.synthetic.main.fragment_memo_list.*

/**
 * A simple [Fragment] subclass.
 */
class MemoListFragment : Fragment() {

    private lateinit var listAdapter: MemoListAdapter
    private var viewModel: MemoListViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_memo_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = activity!!.application!!.let {
            ViewModelProvider(
                activity!!.viewModelStore,
                ViewModelProvider.AndroidViewModelFactory(it)
            )
                .get(MemoListViewModel::class.java)
        }

        viewModel!!.let {
            it.memoListLiveData.value?.let {
                listAdapter = MemoListAdapter(it)
                memoListView.layoutManager =
                    LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
                memoListView.adapter = listAdapter
            }
            listAdapter.itemClickListener = {
                val intent = Intent(activity, DetailMemoActivity::class.java)
                intent.putExtra("memoId", it)
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        listAdapter?.notifyDataSetChanged()
    }
}
