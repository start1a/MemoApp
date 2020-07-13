package com.start3a.memoji.views.Category

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.start3a.memoji.R
import com.start3a.memoji.data.Category
import com.start3a.memoji.viewmodel.CategoryViewModel
import kotlinx.android.synthetic.main.activity_category.*

class CategoryActivity : AppCompatActivity() {

    private var viewModel: CategoryViewModel? = null
    private var listCatEditAdapter: CategoryAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.category)
        }

        viewModel = application!!.let {
            ViewModelProvider(viewModelStore, ViewModelProvider.AndroidViewModelFactory(it))
                .get(CategoryViewModel::class.java).apply {
                    curMemoCat = intent.getStringExtra("curMemoCat")
                }
        }

        viewModel!!.let { VM ->
            // 카테고리가 존재하는가
            VM.categoryLiveData.observe(this, Observer {
                if (it.size > 0) textCatisExists.visibility = View.GONE
                else textCatisExists.visibility = View.VISIBLE
            })

            // 리스트 초기화
            listCatEditAdapter =
                CategoryAdapter(VM.categoryLiveData.value!!)
            listViewCat.adapter = listCatEditAdapter
            listViewCat.layoutManager = LinearLayoutManager(this)

            // 아이템 클릭
            listCatEditAdapter?.itemClickListener = { selectedCat ->
                // 카테고리 선택 모드
                if (VM.curMemoCat != null) {
                    VM.curMemoCat = selectedCat.nameCat
                    resultCategory()
                    finish()
                }
                // 카테고리 관리 모드
                else {
                    dialogSetData("카테고리 수정") { newName ->
                        if (VM.isExistCategory(newName) && newName.isNotEmpty()) {
                            VM.updateCategory(selectedCat, newName)
                        } else Toast.makeText(this, "이미 카테고리가 존재합니다.", Toast.LENGTH_LONG).show()
                    }
                }
            }

            // 카테고리 삭제
            listCatEditAdapter?.itemDeleteListener = { selectedCat ->
                if (VM.curMemoCat != null && VM.curMemoCat == selectedCat.nameCat) {
                    VM.curMemoCat = ""
                }
                VM.deleteCategory(selectedCat)
                listCatEditAdapter?.notifyDataSetChanged()
            }

            // 카테고리 추가
            btnAddCat.setOnClickListener {
                dialogSetData("새 카테고리 추가") { newName ->
                    if (VM.isExistCategory(newName) && newName.isNotEmpty()) {
                        val newCat = Category(nameCat = newName)
                        VM.addCategory(newCat)
                    } else Toast.makeText(this, "이미 카테고리가 존재합니다.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                resultCategory()
                finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        resultCategory()
        super.onBackPressed()
    }

    private fun dialogSetData(title: String, setDataListener: (String) -> Unit) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_data, null)
        val edit = view.findViewById<EditText>(R.id.editURL)
        val alertDialog = AlertDialog.Builder(this)

        viewModel!!.dialogInterface = alertDialog
            .setView(view)
            .setTitle(title)
            .show()

        view.findViewById<Button>(R.id.btnDone).setOnClickListener {
            setDataListener(edit.text.toString())
            viewModel!!.dialogInterface?.dismiss()
            listCatEditAdapter?.notifyDataSetChanged()
        }
    }

    private fun resultCategory() {
        val intent = Intent().apply {
            putExtra("selectedCat", viewModel!!.curMemoCat)
        }
        setResult(Activity.RESULT_OK, intent)
    }
}