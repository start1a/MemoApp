package com.example.memoappexam.views

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
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
import kotlinx.android.synthetic.main.activity_edit_memo.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class EditMemoActivity : AppCompatActivity() {

    private var viewModel: DetailViewModel? = null

    // 프래그먼트
    private lateinit var fragText: MemoTextFragment
    private lateinit var fragImage: MemoImageFragment

    // 이미지 처리 데이터
    private lateinit var currentPhotoPath: String
    private lateinit var photoURI: Uri
    private val REQUEST_IMAGE_GALLERY = 0
    private val REQUEST_IMAGE_CAMERA = 1
    private val REQUEST_IMAGE_URL = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_memo)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        if (viewModel == null) {
            viewModel = application!!.let {
                ViewModelProvider(viewModelStore, ViewModelProvider.AndroidViewModelFactory(it))
                    .get(DetailViewModel::class.java)
            }
        }
        fragText = MemoTextFragment()
        fragImage = MemoImageFragment()

        viewModel!!.let {
            setFragment(it.fragBtnClicked.value!!)

            val id = intent.getStringExtra("memoId")
            if (id != null && it.memoId == null) it.Load_MemoData(id)
        }

        btnFragText.setOnClickListener { ReplaceFragment(it.id) }
        btnFragImage.setOnClickListener { ReplaceFragment(it.id) }
    }

    fun ReplaceFragment(id: Int) {
        viewModel!!.let {
            setFragment(id)
            it.setFragBtn(id)
            it.setEditMode(false)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        viewModel!!.Update_MemoData()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        viewModel!!.let {
            it.mMenu = menu
            it.editMode.observe(this, androidx.lifecycle.Observer {
                viewModel!!.mMenu?.clear()
                supportActionBar?.setDisplayHomeAsUpEnabled(it)
                if (it) menuInflater.inflate(R.menu.menu_delete_memo, viewModel!!.mMenu)
                else menuView()
            })
            it.fragBtnClicked.observe(this, androidx.lifecycle.Observer {
                viewModel!!.mMenu?.clear()
                menuView()
            })
            return true
        }
    }

    fun menuView() {
        when (viewModel!!.fragBtnClicked.value!!) {
            R.id.btnFragText -> menuInflater.inflate(R.menu.menu_text_memo, viewModel!!.mMenu)
            R.id.btnFragImage -> menuInflater.inflate(R.menu.menu_image_memo, viewModel!!.mMenu)
        }
    }

    // 메뉴 버튼이 눌림
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.action_edit -> {
                viewModel!!.editMode.value = true
                viewModel!!.deleteImageListListener = { DialogDeleteMemo() }
            }

            android.R.id.home -> viewModel!!.editMode.value = false

            R.id.action_delete -> DialogDeleteMemo()

            R.id.action_insert_image -> {
                val view = LayoutInflater.from(this).inflate(R.layout.dialog_insert_image, null)

                view.findViewById<Button>(R.id.btnGallery).setOnClickListener {
                    val intent = Intent(
                        Intent.ACTION_OPEN_DOCUMENT,
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    )
                    intent.type = "image/*"
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    startActivityForResult(
                        Intent.createChooser(intent, "Select Picture"),
                        REQUEST_IMAGE_GALLERY
                    )
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
            }

            R.id.action_delete_image -> viewModel!!.editMode.value = true

            R.id.action_delete_image_confirm -> viewModel!!.deleteImageListListener()

            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_IMAGE_GALLERY -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val uri = data.data
                    val clipData = data.clipData

                    if (clipData != null) {
                        for (i in 0..clipData.itemCount - 1) {
                            val item = clipData.getItemAt(i).uri
                            viewModel!!.add_ImageMemoDataList(item.toString())
                        }
                    } else if (uri != null) {
                        viewModel!!.add_ImageMemoDataList(uri.toString())
                    }
                }
            }

            REQUEST_IMAGE_CAMERA -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    viewModel!!.add_ImageMemoDataList(photoURI.toString())
                }
            }

            REQUEST_IMAGE_URL -> {

            }
        }
    }

    fun DialogDeleteMemo() {
        AlertDialog.Builder(this)
            .setTitle("메모를 삭제하시겠습니까?")
            .setNegativeButton("취소", null)
            .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, i ->
                val id = viewModel!!.memoId
                if (id != null) viewModel!!.Delete_MemoData(id)
                finish()
            }).show()
    }

    fun View.showKeyboard(on: Boolean) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (on) imm.toggleSoftInput(
            InputMethodManager.SHOW_FORCED,
            InputMethodManager.HIDE_IMPLICIT_ONLY
        )
        else imm.hideSoftInputFromWindow(windowToken, 0)
    }

    fun setFragment(type: Int) {
        when (type) {
            R.id.btnFragText -> supportFragmentManager.beginTransaction().replace(
                R.id.memoDetailLayout,
                fragText
            ).commit()
            R.id.btnFragImage -> supportFragmentManager.beginTransaction().replace(
                R.id.memoDetailLayout,
                fragImage
            ).commit()
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