package com.start3a.memoji

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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.start3a.memoji.data.Category
import com.start3a.memoji.viewmodel.CategoryViewModel
import kotlinx.android.synthetic.main.activity_category.*

class CategoryActivity : AppCompatActivity() {

    private var viewModel: CategoryViewModel? = null
    private var listCatEditAdapter: CategoryAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = application!!.let {
            ViewModelProvider(viewModelStore, ViewModelProvider.AndroidViewModelFactory(it))
                .get(CategoryViewModel::class.java).apply {
                    memoID = intent.getStringExtra("memoID")
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
            listCatEditAdapter?.itemClickListener = { cat ->
                if (VM.memoID == null) {
                    dialogSetData("카테고리 수정") { newName ->
                        if (VM.isExistCategory(newName) && newName.isNotEmpty()) {
                            VM.updateCategory(cat, newName)
                        } else Toast.makeText(this, "이미 카테고리가 존재합니다.", Toast.LENGTH_LONG).show()
                    }
                }
                else {
                    val intent = Intent().apply {
                        putExtra("selectedCat", cat.nameCat)
                    }
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }

            // 카테고리 삭제
            listCatEditAdapter?.itemDeleteListener = { cat ->
                VM.deleteCategory(cat)
                listCatEditAdapter?.notifyDataSetChanged()
            }

            // 카테고리 추가
            btnAddCat.setOnClickListener {
                dialogSetData("새 카테고리 추가") { newName ->
                    if (VM.isExistCategory(newName) && newName.isNotEmpty()) {
                        val cat = Category(nameCat = newName)
                        VM.addCategory(cat)
                    } else Toast.makeText(this, "이미 카테고리가 존재합니다.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                setResult(Activity.RESULT_OK)
                finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
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
}