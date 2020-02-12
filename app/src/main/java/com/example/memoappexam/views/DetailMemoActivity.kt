package com.example.memoappexam.views

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.memoappexam.R
import com.example.memoappexam.viewmodel.DetailViewModel
import io.realm.RealmList
import kotlinx.android.synthetic.main.activity_detail_memo.*
import kotlinx.android.synthetic.main.content_detail_memo.*

class DetailMemoActivity : AppCompatActivity() {

    private var viewModel: DetailViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_memo)
        setSupportActionBar(toolbar)

        viewModel = application!!.let {
            ViewModelProvider(viewModelStore, ViewModelProvider.AndroidViewModelFactory(it))
                .get(DetailViewModel::class.java)
        }
        viewModel!!.let {
            it.title.observe(this, Observer { editTitle.setText(it) })
            it.content.observe(this, Observer { editContent.setText(it) })
            it.image.observe(this, Observer {
                if (it.size > 0) {
                    Glide.with(imgMemo)
                        .load(it[0])
                        .into(imgMemo)
                } else {
                    Glide.with(imgMemo)
                        .load(R.drawable.ic_launcher_background)
                        .into(imgMemo)
                }
            })
        }

        val id = intent.getStringExtra("memoId")
        if (id != null) viewModel!!.Load_MemoData(id)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        viewModel?.Update_MemoData(editTitle.text.toString(),
            editContent.text.toString(),
            RealmList("https://grepp-programmers-challenges.s3.ap-northeast-2.amazonaws.com/2020-flo/cover.jpg"))
    }
}
