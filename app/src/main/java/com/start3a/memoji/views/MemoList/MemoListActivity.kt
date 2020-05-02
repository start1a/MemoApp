package com.start3a.memoji.views.MemoList

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.start3a.memoji.R
import com.start3a.memoji.viewmodel.MemoListViewModel
import com.start3a.memoji.views.EditMemo.EditMemoActivity
import kotlinx.android.synthetic.main.activity_memo_list.*

class MemoListActivity : AppCompatActivity() {

    private var viewModel: MemoListViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memo_list)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        viewModel = application!!.let {
            ViewModelProvider(viewModelStore, ViewModelProvider.AndroidViewModelFactory(it))
                .get(MemoListViewModel::class.java)
        }

        fab.setOnClickListener {
            val intent = Intent(applicationContext, EditMemoActivity::class.java)
            startActivity(intent)
        }

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.memoListLayout,
            MemoListFragment()
        )
        fragmentTransaction.commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        viewModel!!.let {
            it.mMenu = menu
            menuInflater.inflate(R.menu.menu_memo_list, it.mMenu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        viewModel!!.let { VM ->

            when (item.itemId) {

                R.id.action_layout_configuration -> {
                    // 레이아웃 구성 변경
                    // 메뉴 아이콘 변경
                    if (VM.layout_memoList.value == VM.LAYOUT_LINEAR) {
                        VM.setLayoutMemoList(VM.LAYOUT_GRID)
                        VM.mMenu?.findItem(R.id.action_layout_configuration)
                            ?.setIcon(R.drawable.ic_view_linear_list_black_24dp)
                    } else if (VM.layout_memoList.value == VM.LAYOUT_GRID) {
                        VM.setLayoutMemoList(VM.LAYOUT_LINEAR)
                        VM.mMenu?.findItem(R.id.action_layout_configuration)
                            ?.setIcon(R.drawable.ic_view_grid_black_24dp)
                    }
                }

            }
            return true
        }
    }
}