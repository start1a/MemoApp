package com.start3a.memoji.views.MemoList


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.start3a.memoji.R
import com.start3a.memoji.data.MemoData
import com.start3a.memoji.viewmodel.MemoListViewModel
import com.start3a.memoji.views.EditMemo.EditMemoActivity
import kotlinx.android.synthetic.main.fragment_memo_list.*

class MemoListFragment : Fragment() {

    // 어댑터
    private lateinit var listAdapterLinear: MemoListAdapter
    private lateinit var listAdapterGrid: MemoListAdapter
    private var curAdapter: MemoListAdapter? = null
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

        viewModel!!.let { VM ->
            initAdapter(VM.memoListLiveData.value!!)
            VM.layoutMemoList.observe(viewLifecycleOwner, Observer {
                setLayoutAdapter()
            })
            VM.listNotifyListener = {
                curAdapter?.notifyDataSetChanged()
            }
            VM.newMemoListQueryListener = {
                initAdapter(it)
                setLayoutAdapter()
                curAdapter?.notifyDataSetChanged()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        curAdapter?.notifyDataSetChanged()
    }

    private fun initAdapter(list: MutableList<MemoData>) {
        listAdapterLinear = MemoListAdapter(list, R.layout.item_memo)
        listAdapterGrid = MemoListAdapter(list, R.layout.item_memo_card)
        listAdapterLinear.itemClickListener = {
            setItemClickListener(it)
        }
        listAdapterGrid.itemClickListener = {
            setItemClickListener(it)
        }
    }

    private fun setLayoutAdapter() {
        viewModel!!.let { VM ->
            when (VM.layoutMemoList.value!!) {
                VM.LAYOUT_LINEAR -> {
                    curAdapter = listAdapterLinear
                    memoListView.layoutManager =
                        LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
                }
                VM.LAYOUT_GRID -> {
                    curAdapter = listAdapterGrid
                    memoListView.layoutManager =
                        StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                }
            }
            memoListView.adapter = curAdapter
        }
    }

    private fun setItemClickListener(id: String) {
        val intent = Intent(activity, EditMemoActivity::class.java)
        intent.putExtra("MEMO_ID", id)
        startActivity(intent)
    }
}