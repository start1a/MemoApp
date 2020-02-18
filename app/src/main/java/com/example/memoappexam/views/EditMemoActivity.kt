package com.example.memoappexam.views

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.PersistableBundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.example.memoappexam.R
import com.example.memoappexam.viewmodel.DetailViewModel
import io.realm.RealmList
import kotlinx.android.synthetic.main.activity_edit_memo.*
import kotlinx.android.synthetic.main.fragment_memo_text.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class EditMemoActivity : AppCompatActivity() {

    private var mMenu: Menu? = null
    private var viewModel: DetailViewModel? = null
    private lateinit var currentPhotoPath: String
    private lateinit var photoURI: Uri

    private val fragmentManager = supportFragmentManager
    private val fragText: MemoTextFragment by lazy {
        val fragment = MemoTextFragment()
        fragmentManager.beginTransaction().add(R.id.memoDetailLayout, fragment).commit()
        fragment
    }
    private val fragImage: MemoImageFragment by lazy {
        val fragment = MemoImageFragment()
        fragmentManager.beginTransaction().add(R.id.memoDetailLayout, fragment).commit()
        fragment
    }

    private val REQUEST_IMAGE_GALLERY = 0
    private val REQUEST_IMAGE_CAMERA = 1
    private val REQUEST_IMAGE_URL = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_memo)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        // 뷰 모델 생성
        viewModel = application!!.let {
            ViewModelProvider(viewModelStore, ViewModelProvider.AndroidViewModelFactory(it))
                .get(DetailViewModel::class.java)
        }

        // 메모 텍스트 프래그먼트 생성
        fragmentManager.beginTransaction().replace(R.id.memoDetailLayout, fragText).commit()
        // 기존 데이터 로드
        viewModel!!.let {
            val id = intent.getStringExtra("memoId")
            if (id != null) it.Load_MemoData(id)
        }
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (editContent.text.count() > 0 || editTitle.text.count() > 0 || viewModel!!.image.value?.size ?: 0 > 0) {
            viewModel!!.Update_MemoData(
                editTitle.text.toString(),
                editContent.text.toString(),
                viewModel!!.image.value ?: RealmList()
            )
        }
    }

    // 최초 메뉴 키가 눌렸을 시 호출
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // return super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_detail_memo, menu)
        mMenu = menu
        EditMode(false)
        return true
    }

    // 메뉴 버튼이 눌림
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
                AlertDialog.Builder(this)
                    .setTitle("메모를 삭제하시겠습니까?")
                    .setNegativeButton("취소", null)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, i ->
                        val id = viewModel!!.memoId
                        if (id != null) viewModel!!.Delete_MemoData(id)
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
                    val permissioncheck =
                        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)

                    if (permissioncheck == PackageManager.PERMISSION_DENIED) {
                        val requiredPermissions = arrayOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                        ActivityCompat.requestPermissions(this, requiredPermissions, 1)

                    } else {
                        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                            takePictureIntent.resolveActivity(packageManager)?.also {
                                val photoFile: File? = try {
                                    createImageFile()
                                } catch (ex: IOException) {
                                    null
                                }
                                photoFile?.also {
                                    photoURI = FileProvider.getUriForFile(
                                        this,
                                        "com.example.memoappexam",
                                        it
                                    )
                                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAMERA)
                                }
                            }
                        }
                    }
                }

                view.findViewById<Button>(R.id.btnURL).setOnClickListener {

                }

                AlertDialog.Builder(this).setView(view)
                    .setTitle("이미지 추가")
                    .show()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_IMAGE_GALLERY -> {
                if (resultCode == Activity.RESULT_OK && data != null && data.data != null) {
                    viewModel!!.add_ImageMemoData(data.data.toString())
                }
            }

            REQUEST_IMAGE_CAMERA -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    viewModel!!.add_ImageMemoData(photoURI.toString())
                }
            }

            REQUEST_IMAGE_URL -> {

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
        editTitle?.let {
            it.isFocusableInTouchMode = on
            it.isFocusable = on
            it.hideKeyboard(!on)
        }
        // 내용
        editContent?.let {
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

    fun onClick(view: View) {
        when (view.id) {
            // 텍스트
            R.id.btnFragText -> {
                fragmentManager.beginTransaction().hide(fragImage).commit()
                fragmentManager.beginTransaction().show(fragText).commit()
            }

            // 이미지
            R.id.btnFragImage -> {
                fragmentManager.beginTransaction().hide(fragText).commit()
                fragmentManager.beginTransaction().show(fragImage).commit()
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }
}