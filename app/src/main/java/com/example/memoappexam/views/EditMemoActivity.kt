package com.example.memoappexam.views

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.memoappexam.ImageListAdapter
import com.example.memoappexam.R
import com.example.memoappexam.viewmodel.DetailViewModel
import io.realm.RealmList
import kotlinx.android.synthetic.main.activity_edit_memo.*
import kotlinx.android.synthetic.main.content_edit_memo.*
import java.util.zip.Inflater

class EditMemoActivity : AppCompatActivity() {

    private val id = intent.getStringExtra("memoId")
    private var mMenu: Menu? = null
    private var viewModel: DetailViewModel? = null
    private lateinit var listImageAdapter: ImageListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_memo)
        setSupportActionBar(toolbar)

        viewModel = application!!.let {
            ViewModelProvider(viewModelStore, ViewModelProvider.AndroidViewModelFactory(it))
                .get(DetailViewModel::class.java)
        }
        viewModel!!.let {
            it.image.value?.let {
                listImageAdapter = ImageListAdapter(it)
                imageMemoListView.adapter = listImageAdapter
                imageMemoListView.layoutManager = GridLayoutManager(this, 3)
            }

            it.title.observe(this, Observer { editTitle.setText(it) })
            it.content.observe(this, Observer { editContent.setText(it) })
            it.image.observe(this, Observer { listImageAdapter.notifyDataSetChanged() })
        }

        if (id != null) viewModel!!.Load_MemoData(id)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        viewModel?.Update_MemoData(
            editTitle.text.toString(),
            editContent.text.toString(),
            RealmList("https://grepp-programmers-challenges.s3.ap-northeast-2.amazonaws.com/2020-flo/cover.jpg")
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // return super.onCreateOptionsMenu(menu)
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.menu_detail_memo, menu)
        mMenu = menu
        // 처음은 수정 삭제 모드
        mMenu?.findItem(R.id.action_insert_image)?.setVisible(false)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_edit -> {
                EditMode(true)
                return true
            }

            R.id.action_delete -> {
                val view = LayoutInflater.from(this).inflate(R.layout.dialog_delete_memodata, null)

                AlertDialog.Builder(this)
                    .setTitle("메모를 삭제하시겠습니까?")
                    .setView(view)
                    .setNegativeButton("취소", null)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, i ->
                        viewModel!!.Delete_MemoData(id)
                        finish()
                    }).show()

                return true
            }

            android.R.id.home -> {
                EditMode(false)
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    fun EditMode(on: Boolean) {
        mMenu?.let {
            it.findItem(R.id.action_edit)?.setVisible(!on)
            it.findItem(R.id.action_delete)?.setVisible(!on)
            it.findItem(R.id.action_insert_image)?.setVisible(on)
        }
        // 상단 메뉴 뒤로가기
        supportActionBar?.setDisplayHomeAsUpEnabled(on)
    }
}