package com.example.memoappexam.views

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
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
import kotlinx.android.synthetic.main.content_edit_memo.*
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.net.ssl.HttpsURLConnection
import kotlin.coroutines.CoroutineContext

class EditMemoActivity : AppCompatActivity(), CoroutineScope {

    private var viewModel: DetailViewModel? = null
    var dialogInterfaceLoading: DialogInterface? = null

    // 코루틴
    private lateinit var mJob: Job
    private val handler = CoroutineExceptionHandler { _, throwable ->
        Log.e("Exception", ":" + throwable)
    }
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + mJob

    // 프래그먼트
    private lateinit var fragText: MemoTextFragment
    private lateinit var fragImage: MemoImageFragment

    // 이미지 처리 데이터
    private lateinit var currentPhotoPath: String
    private lateinit var photoURI: Uri

    // private lateinit var photoURI: Uri
    private val REQUEST_IMAGE_GALLERY = 0
    private val REQUEST_IMAGE_CAMERA = 1

    // 새 메모? 기존 메모? 체크 (새 메모 = null)
    private var id: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_memo)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        mJob = Job()

        viewModel = application!!.let { app ->
            ViewModelProvider(viewModelStore, ViewModelProvider.AndroidViewModelFactory(app))
                .get(DetailViewModel::class.java).also { VM ->
                    id = intent.getStringExtra("memoId")
                    if (id != null)
                        VM.Load_MemoData(id!!)
                    VM.context = applicationContext
                    VM.setEditable(id == null)
                }
        }
        fragText = MemoTextFragment()
        fragImage = MemoImageFragment()
        setFragment()

        bottom__navigation_edit_memo.setOnNavigationItemSelectedListener { menu ->
            when (menu.itemId) {
                // 텍스트
                R.id.action_fragment_text -> {
                    // 하단 탭 클릭 시
                    // 새 메모 : 항상 수정 모드
                    // 기존 메모 : 항상 보기 모드
                    ReplaceFragment(menu.itemId, viewModel!!.let { VM ->
                        VM.titleTemp.isNotEmpty() || VM.contentTemp.isNotEmpty()
                    })
                }
                // 이미지
                R.id.action_fragment_image -> {
                    ReplaceFragment(menu.itemId, false)
                }
            }
            return@setOnNavigationItemSelectedListener true
        }
    }

    private fun ReplaceFragment(id: Int, editable: Boolean) {
        viewModel!!.let {
            it.setFragBtn(id)
            setFragment()
            it.setEditable(editable)
        }
    }

    override fun onBackPressed() {
        launch {
            Loading_SaveMemo()
            viewModel!!.Update_MemoData()
            dialogInterfaceLoading?.dismiss()
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mJob.cancel()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        viewModel!!.let {
            it.mMenu = menu
            it.editable.observe(this, androidx.lifecycle.Observer {
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

    private fun menuView() {
        when (viewModel!!.fragBtnClicked.value!!) {
            R.id.action_fragment_text -> menuInflater.inflate(
                R.menu.menu_text_memo,
                viewModel!!.mMenu
            )
            R.id.action_fragment_image -> menuInflater.inflate(
                R.menu.menu_image_memo,
                viewModel!!.mMenu
            )
        }
    }

    // 메뉴 버튼이 눌림
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.action_edit -> {
                viewModel!!.editable.value = true
                viewModel!!.deleteImageListListener = { DialogDeleteMemo() }
            }

            android.R.id.home -> viewModel!!.editable.value = false

            R.id.action_delete -> DialogDeleteMemo()

            R.id.action_insert_image -> {

                var dialogInterface: DialogInterface? = null
                val alertDialog = AlertDialog.Builder(this)

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

                    val permissionCheck =
                        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)

                    if (permissionCheck == PackageManager.PERMISSION_DENIED) {
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
                                        "com.example.memoappexam.fileprovider",
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
                    dialogInterface?.dismiss()
                    val view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_url, null)
                    val editUrl = view.findViewById<EditText>(R.id.editURL)

                    dialogInterface = alertDialog
                        .setView(view)
                        .setTitle("URL을 입력하세요")
                        .show()

                    view.findViewById<Button>(R.id.btnURLImageAdd).setOnClickListener {
                        val url = editUrl.text.toString()
                        launch(handler) {
                            Loading_SaveMemo()
                            if (GetImageFromURL(url)) {
                                val list = listOf<Uri>(
                                    Uri.parse(editUrl.text.toString())
                                )
                                viewModel!!.add_ImageMemoDataList(list)
                                Toast.makeText(applicationContext, "이미지 업로드 완료", Toast.LENGTH_SHORT)
                                    .show()
                            } else Toast.makeText(
                                applicationContext,
                                "잘못된 URL입니다",
                                Toast.LENGTH_SHORT
                            ).show()
                            dialogInterfaceLoading?.dismiss()
                        }
                        dialogInterface?.dismiss()
                    }
                }
                dialogInterface = alertDialog
                    .setView(view)
                    .setTitle("이미지 추가")
                    .show()
            }

            R.id.action_delete_image -> viewModel!!.setEditable(true)

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
                    val list = mutableListOf<Uri>()
                    // 2개 이상
                    if (clipData != null) {
                        var numNullBitmap = 0
                        for (i in 0 until clipData.itemCount) {
                            val item = clipData.getItemAt(i).uri
                            if (item != null) list.add(item)
                            else ++numNullBitmap
                        }
                        if (numNullBitmap > 0)
                            Toast.makeText(
                                this,
                                numNullBitmap.toString() + "개의 이미지 파일 불러오기 실패",
                                Toast.LENGTH_LONG
                            ).show()
                        viewModel!!.add_ImageMemoDataList(list)
                    }
                    // 1개
                    else if (uri != null) {
                        list.add(uri)
                        viewModel!!.add_ImageMemoDataList(list)
                    }
                }
            }

            REQUEST_IMAGE_CAMERA -> {
                if (resultCode == Activity.RESULT_OK) {
                    viewModel!!.add_ImageMemoDataList(listOf(photoURI))
                } else Toast.makeText(this, "촬영 이미지 불러오기 실패", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun DialogDeleteMemo() {
        AlertDialog.Builder(this)
            .setTitle("메모를 삭제하시겠습니까?")
            .setNegativeButton("취소", null)
            .setPositiveButton("확인") { _, _ ->
                viewModel!!.Delete_MemoData()
                finish()
            }.show()
    }

    private fun setFragment() {
        when (viewModel!!.fragBtnClicked.value) {
            R.id.action_fragment_text ->
                supportFragmentManager.beginTransaction().replace(
                    R.id.memoDetailLayout,
                    fragText
                ).commit()
            R.id.action_fragment_image ->
                supportFragmentManager.beginTransaction().replace(
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

    private fun Loading_SaveMemo() {
        val alertDialog = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_progress_bar_for_waiting, null)
        dialogInterfaceLoading = alertDialog
            .setView(view)
            .setCancelable(false)
            .show()
    }

    suspend fun GetImageFromURL(urlInput: String): Boolean {
        var code = 0
        withContext(Dispatchers.IO) {
            lateinit var con: HttpsURLConnection

            var url = URL(urlInput)
            con = url.openConnection() as HttpsURLConnection
            con.connect()

            code = con.responseCode
            con.disconnect()
        }
        return code == 200
    }
}