package com.example.memoappexam.views

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.memoappexam.R
import com.example.memoappexam.viewmodel.MemoListViewModel

import kotlinx.android.synthetic.main.activity_memo_list.*

class MemoListActivity : AppCompatActivity() {

    private var viewModel: MemoListViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memo_list)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        fab.setOnClickListener { view ->
            val intent = Intent(applicationContext, EditMemoActivity::class.java)
            startActivity(intent)
        }

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.memoListLayout, MemoListFragment())
        fragmentTransaction.commit()

        viewModel = application!!.let {
            ViewModelProvider(viewModelStore, ViewModelProvider.AndroidViewModelFactory(it))
                .get(MemoListViewModel::class.java)
        }
    }
}
