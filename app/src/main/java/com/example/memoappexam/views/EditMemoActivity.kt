package com.example.memoappexam.views

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.memoappexam.ImageListAdapter
import com.example.memoappexam.R
import com.example.memoappexam.viewmodel.DetailViewModel
import io.realm.Realm
import io.realm.RealmList
import kotlinx.android.synthetic.main.activity_edit_memo.*
import kotlinx.android.synthetic.main.content_edit_memo.*

class EditMemoActivity : AppCompatActivity() {

    private var id: String? = null
    private var mMenu: Menu? = null
    private var viewModel: DetailViewModel? = null

    private val REQUEST_IMAGE_GALLERY = 0
    private val REQUEST_IMAGE_CAMERA = 1
    private val REQUEST_IMAGE_URL = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_memo)
        setSupportActionBar(toolbar)

        // 뷰 모델 생성
        viewModel = application!!.let {
            ViewModelProvider(viewModelStore, ViewModelProvider.AndroidViewModelFactory(it))
                .get(DetailViewModel::class.java)
        }
        viewModel!!.let {
            it.title.observe(this, Observer { editTitle.setText(it) })
            it.content.observe(this, Observer { editContent.setText(it) })
        }

        // 이미지 리스트
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentImageList, MemoImageListFragment())
        fragmentTransaction.commit()

        // 기존 데이터 로드
        id = intent.getStringExtra("memoId")
        if (id != null) viewModel!!.Load_MemoData(id?:"")
        // 상세 보기 모드
        EditMode(false)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (editContent.text.count() > 0 || editTitle.text.count() > 0 || viewModel!!.image.value?.size?:0 > 0) {
            viewModel!!.Update_MemoData(
                editTitle.text.toString(),
                editContent.text.toString(),
                viewModel!!.image.value?: RealmList()
            )
        }
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

            android.R.id.home -> {
                EditMode(false)
                return true
            }

            R.id.action_delete -> {
                val view = LayoutInflater.from(this).inflate(R.layout.dialog_insert_image, null)

                AlertDialog.Builder(this)
                    .setTitle("메모를 삭제하시겠습니까?")
                    .setView(view)
                    .setNegativeButton("취소", null)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, i ->
                        viewModel!!.Delete_MemoData(id?:"")
                        finish()
                    }).show()

                return true
            }

            R.id.action_insert_image -> {
                val view = LayoutInflater.from(this).inflate(R.layout.dialog_insert_image, null)

                view.findViewById<Button>(R.id.btnGallery).setOnClickListener {
                    val intent = Intent()
                    intent.type = "image/*"
                    intent.action = Intent.ACTION_GET_CONTENT
                    startActivityForResult(intent, REQUEST_IMAGE_GALLERY)
                }

                view.findViewById<Button>(R.id.btnCamera).setOnClickListener {

                }

                view.findViewById<Button>(R.id.btnURL).setOnClickListener {

                }

                AlertDialog.Builder(this)
                    .setView(view)
                    .setTitle("이미지 추가")
                    .show()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_GALLERY) {
            if (resultCode == Activity.RESULT_OK && data != null && data.data != null) {
                viewModel!!.add_ImageMemoData(data.data.toString())
            }
        }
    }

    fun EditMode(on: Boolean) {
        // 좌측 상단 메뉴
        supportActionBar?.setDisplayHomeAsUpEnabled(on)
        // 우측 상단 메뉴
        mMenu?.let {
            it.findItem(R.id.action_edit)?.setVisible(!on)
            it.findItem(R.id.action_delete)?.setVisible(!on)
            it.findItem(R.id.action_insert_image)?.setVisible(on)
        }
        // 제목
        editTitle.let {
            it.isFocusableInTouchMode = on
            it.isFocusable = on
            //it.hideKeyboard(!on)
        }
        // 내용
        editContent.let {
            it.isFocusableInTouchMode = on
            it.isFocusable = on
            it.hideKeyboard(!on)
        }
    }

    fun View.hideKeyboard(on: Boolean) {
        if (on) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(windowToken, 0)
        }
    }
}