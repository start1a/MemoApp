package com.start3a.memoji.views.EditMemo

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.*
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
import com.start3a.memoji.CategoryActivity
import com.start3a.memoji.R
import com.start3a.memoji.viewmodel.EditMemoViewModel
import com.start3a.memoji.views.EditMemo.Alarm.MemoAlarmFragment
import com.start3a.memoji.views.EditMemo.Image.MemoImageFragment
import com.start3a.memoji.views.EditMemo.Image.Search.SearchImageActivity
import com.start3a.memoji.views.EditMemo.Text.MemoTextFragment
import com.start3a.memoji.views.LoadingProgressBar
import kotlinx.android.synthetic.main.activity_edit_memo.*
import kotlinx.android.synthetic.main.content_edit_memo.*
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.CoroutineContext

class EditMemoActivity : AppCompatActivity(), CoroutineScope {

    // private lateinit var photoURI: Uri
    val REQUEST_IMAGE_GALLERY = 0
    val REQUEST_IMAGE_CAMERA = 1
    val REQUEST_IMAGE_DOWNLOAD = 2
    val REQUEST_SELECT_CATEGORY = 3

    private var viewModel: EditMemoViewModel? = null
    private val dialogCalendar = Calendar.getInstance()
    private var dialogInterface: DialogInterface? = null

    // 코루틴
    private lateinit var mJob: Job
    private val handler = CoroutineExceptionHandler { _, throwable ->
        Log.e("Exception", ":$throwable")
    }
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + mJob

    // 프래그먼트

    // 이미지 처리 데이터
    private lateinit var currentPhotoPath: String
    private lateinit var photoURI: Uri

    // 새 메모? 기존 메모? 체크 (새 메모 = null)
    private var id: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_memo)
        setSupportActionBar(toolbarEdit)
        mJob = Job()

        viewModel = application!!.let { app ->
            ViewModelProvider(viewModelStore, ViewModelProvider.AndroidViewModelFactory(app))
                .get(EditMemoViewModel::class.java).also { VM ->
                    id = intent.getStringExtra("MEMO_ID")
                    VM.context = applicationContext
                    if (id != null)
                        VM.Load_MemoData(id!!)
                    VM.setEditable(id == null)
                    // 액티비티, 프래그먼트 모두 datePicker 호출 가능
                    VM.datePickerShowListener = { openDateDialog() }
                    VM.category.value = VM.memoData.category
                }
        }
        setFragment()

        // 카테고리
        viewModel!!.category.observe(this, androidx.lifecycle.Observer {
            if (it.isEmpty()) supportActionBar?.title = "카테고리 없음"
            else supportActionBar?.title = it
        })

        // 하단 탭 클릭 시
        bottom__navigation_edit_memo.setOnNavigationItemSelectedListener { menu ->
            when (menu.itemId) {
                // 텍스트
                R.id.action_fragment_text -> {
                    ReplaceFragment(menu.itemId, viewModel!!.let { VM ->
                        // 새 메모 : 항상 수정 모드
                        // 기존 메모 : 항상 보기 모드
                        VM.memoId == null && (VM.titleTemp.isEmpty() || VM.contentTemp.isEmpty())
                    })
                }
                // 이미지
                R.id.action_fragment_image -> {
                    ReplaceFragment(menu.itemId, false)
                }
                // 알람
                R.id.action_fragment_alarm -> {
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
            LoadingProgressBar.Progress_ProcessingData(this@EditMemoActivity)
            viewModel!!.AddOrUpdate_MemoData()
            LoadingProgressBar.dialogInterfaceLoading?.dismiss()
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mJob.cancel()
        dialogInterface?.dismiss()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        viewModel!!.let { VM ->
            VM.mMenu = menu
            VM.fragBtnClicked.observe(this, androidx.lifecycle.Observer {
                viewModel!!.mMenu?.clear()
                menuView()
            })
            VM.editable.observe(this, androidx.lifecycle.Observer {
                viewModel!!.mMenu?.clear()
                supportActionBar?.setDisplayHomeAsUpEnabled(it)
                if (it) menuInflater.inflate(R.menu.menu_delete_memo, VM.mMenu)
                else menuView()
            })
            return true
        }
    }

    private fun menuView() {
        viewModel!!.fragBtnClicked.value?.let {
            val menu = viewModel!!.mMenu
            when (it) {
                R.id.action_fragment_text -> menuInflater.inflate(
                    R.menu.menu_text_memo,
                    menu
                )
                R.id.action_fragment_image -> menuInflater.inflate(
                    R.menu.menu_image_memo,
                    menu
                )
                R.id.action_fragment_alarm -> menuInflater.inflate(
                    R.menu.menu_alarm_memo,
                    menu
                )
            }
        }
    }

    // 메뉴 버튼이 눌림
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        viewModel!!.let { VM ->
            when (item.itemId) {

                R.id.action_edit -> VM.setEditable(true)

                android.R.id.home -> VM.setEditable(false)

                R.id.action_share -> menuShareMemo()

                R.id.action_delete -> DialogDeleteMemo()

                R.id.action_category -> DialogSelectCategory()

                R.id.action_add_image -> menuAddImage()

                R.id.action_delete_image -> VM.setEditable(true)

                R.id.action_delete_sweep_content -> VM.allContentSelectListener()

                R.id.action_delete_data -> {
                    when (VM.fragBtnClicked.value) {
                        R.id.action_fragment_text -> DialogDeleteMemo()
                        R.id.action_fragment_image -> VM.deleteImageListListener()
                        // R.id.action_fragment_alarm
                        else -> {
                            VM.deleteAlarmListListener()
                        }
                    }
                }

                R.id.action_add_alarm -> openDateDialog()

                R.id.action_delete_alarm -> VM.setEditable(true)


                else -> return super.onOptionsItemSelected(item)
            }
        }
        return true
    }

    private fun DialogSelectCategory() {
        val alertDialog = AlertDialog.Builder(this)

        dialogInterface = alertDialog
            .setTitle("카테고리 선택")
            .setSingleChoiceItems(arrayOf("선택", "제거"), -1) { _, index ->
                if (index == 0) {
                    val intent = Intent(applicationContext, CategoryActivity::class.java).apply {
                        putExtra("memoID", viewModel!!.memoData.id)
                    }
                    startActivityForResult(intent, REQUEST_SELECT_CATEGORY)
                } else viewModel!!.setCategory("")
                dialogInterface?.dismiss()
            }
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_IMAGE_GALLERY -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val uri = data.data
                    val clipData = data.clipData
                    val listUri = mutableListOf<String>()
                    // 2개 이상
                    if (clipData != null) {
                        var numNullBitmap = 0
                        for (i in 0 until clipData.itemCount) {
                            val item = clipData.getItemAt(i).uri
                            if (item != null) listUri.add(item.toString())
                            else ++numNullBitmap
                        }
                        if (numNullBitmap > 0) {
                            Toast.makeText(
                                this,
                                numNullBitmap.toString() + "개의 이미지 파일 불러오기 실패",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        viewModel!!.addImageList(listUri)
                    }
                    // 1개
                    else if (uri != null) {
                        listUri.add(uri.toString())
                        viewModel!!.addImageList(listUri)
                    }
                }
            }

            REQUEST_IMAGE_CAMERA -> {
                if (resultCode == Activity.RESULT_OK) {
                    viewModel!!.addImageList(listOf(photoURI.toString()))
                } else Toast.makeText(this, "촬영 이미지 불러오기 실패", Toast.LENGTH_LONG).show()
            }

            REQUEST_IMAGE_DOWNLOAD -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    data.getStringArrayListExtra("resImages")?.let { list ->
                        val listUri = mutableListOf<String>()
                        list.forEach {
                            listUri.add(it)
                        }
                        viewModel!!.addImageList(listUri)
                    }
                }
            }

            REQUEST_SELECT_CATEGORY -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val nameCat = data.getStringExtra("selectedCat") ?: ""
                    viewModel!!.setCategory(nameCat)
                }
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
                    MemoTextFragment()
                ).commit()
            R.id.action_fragment_image ->
                supportFragmentManager.beginTransaction().replace(
                    R.id.memoDetailLayout,
                    MemoImageFragment()
                ).commit()
            R.id.action_fragment_alarm ->
                supportFragmentManager.beginTransaction().replace(
                    R.id.memoDetailLayout,
                    MemoAlarmFragment()
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

    suspend fun GetImageFromURL(urlInput: String): Boolean {
        var code = 0
        withContext(Dispatchers.IO) {
            val con = URL(urlInput).openConnection() as HttpURLConnection
            con.run {
                code = responseCode
                disconnect()
            }
        }
        return code == 200
    }

    private fun ShareImages() {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND_MULTIPLE
            type = "image/*"
            // 이미지 Uri 리스트
            val list = arrayListOf<Uri>().apply {
                viewModel!!.imageFileLinks.value?.let { images ->
                    for (item in images) {
                        // 저장되지 않은 이미지?
                        // 원본 파일도 없다면 이미지가 출력되지 않는 에러 발생
                        if (item.originalPath.isEmpty())
                            add(Uri.parse(item.uri))
                        else add(
                            // FileUriExposedException : exposed beyond app through ClipData.Item.getUri()
                            // Android 7.0 이후 file:/// URI 가 직접 노출되지 않도록 content:// URI를 보내고
                            // 이에 대해서 임시 액세스 권한을 부여하는 방식으로 변경
                            FileProvider.getUriForFile(
                                applicationContext,
                                "com.start3a.memoji.fileprovider",
                                File(item.originalPath)
                            )
                        )
                    }
                }
            }
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, list)
        }
        startActivity(Intent.createChooser(intent, "Share"))
    }

    // 알람 추가용 날짜 다이얼로그
    // 디폴트 매개변수 : 새 알람 추가용
    // 새로운 매개변수 : 기존 알람 시간 변경용
    private fun openDateDialog(index: Int = -1, date: Date = Date()) {
        val datePickerDialog = DatePickerDialog(this)
        dialogCalendar.time = date
        datePickerDialog.setOnDateSetListener { view, year, month, dayOfMonth ->
            dialogCalendar.set(year, month, dayOfMonth)
            openTimeDialog(index)
        }
        datePickerDialog.show()
    }

    private fun openTimeDialog(index: Int) {
        val timePickerDialog = TimePickerDialog(
            this,
            TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                dialogCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                dialogCalendar.set(Calendar.MINUTE, minute)

                if (!dialogCalendar.time.after(Date())) {
                    Toast.makeText(this, "현재 이후의 시간만 등록할 수 있습니다", Toast.LENGTH_SHORT).show()
                    return@OnTimeSetListener
                }
                // 새 알람 추가
                if (index == -1) {
                    viewModel?.SetAlarm(dialogCalendar.time)?.let { isSuccess ->
                        if (!isSuccess)
                            Toast.makeText(this, "동일한 알람이 존재합니다", Toast.LENGTH_SHORT).show()
                    }
                }
                // 기존 알람 내용 수정
                else {
                    viewModel?.UpdateAlarm(index, dialogCalendar.time)?.let { isSuccess ->
                        if (!isSuccess)
                            Toast.makeText(this, "동일한 알람이 존재하여 변경되지 않았습니다", Toast.LENGTH_SHORT)
                                .show()
                    }
                }
            },
            0, 0, true
        )
        timePickerDialog.show()
    }

    private fun menuAddImage() {
        val alertDialog = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_insert_image, null)

        // 갤러리
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

        // 카메라
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
                                "com.start3a.memoji.fileprovider",
                                it
                            )
                            takePictureIntent.putExtra(
                                MediaStore.EXTRA_OUTPUT,
                                photoURI
                            )
                            startActivityForResult(
                                takePictureIntent,
                                REQUEST_IMAGE_CAMERA
                            )
                        }
                    }
                }
            }

        }

        // URL에서 가져오기
        view.findViewById<Button>(R.id.btnURL).setOnClickListener {
            dialogInterface?.dismiss()
            val viewUrl = LayoutInflater.from(this).inflate(R.layout.dialog_edit_data, null)
            val editUrl = viewUrl.findViewById<EditText>(R.id.editURL)

            dialogInterface = alertDialog
                .setView(viewUrl)
                .setTitle("URL을 입력하세요")
                .show()

            // 추가
            viewUrl.findViewById<Button>(R.id.btnDone).setOnClickListener {
                val url = editUrl.text.toString()
                launch(handler) {
                    LoadingProgressBar.Progress_ProcessingData(this@EditMemoActivity)
                    if (GetImageFromURL(url)) {
                        val list = listOf(
                            editUrl.text.toString()
                        )
                        viewModel!!.addImageList(list)
                        Toast.makeText(
                            applicationContext,
                            "이미지 업로드 완료",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    } else Toast.makeText(
                        applicationContext,
                        "잘못된 URL입니다",
                        Toast.LENGTH_SHORT
                    ).show()
                    LoadingProgressBar.dialogInterfaceLoading?.dismiss()
                }
                dialogInterface?.dismiss()
            }
        }

        // 이미지 다운로드
        view.findViewById<Button>(R.id.btnSearchDownload).setOnClickListener {
            startActivityForResult(
                Intent(this, SearchImageActivity::class.java),
                REQUEST_IMAGE_DOWNLOAD
            )
        }

        dialogInterface = alertDialog
            .setView(view)
            .setTitle("이미지 추가")
            .show()
    }

    private fun menuShareMemo() {
        viewModel!!.let { VM ->
            val alertDialog = AlertDialog.Builder(this)
            val view = LayoutInflater.from(this).inflate(R.layout.dialog_share_memo, null)

            // 텍스트 공유
            view.findViewById<Button>(R.id.btnShareText).setOnClickListener {
                if (VM.isExistText()) {
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, VM.titleTemp)
                        putExtra(Intent.EXTRA_TEXT, VM.contentTemp)
                    }
                    startActivity(intent)
                } else Toast.makeText(this, "공유할 텍스트가 없습니다.", Toast.LENGTH_SHORT).show()
            }

            // 이미지 공유
            view.findViewById<Button>(R.id.btnShareImage).setOnClickListener {
                if (VM.isExistImage()) {
                    ShareImages()
                } else Toast.makeText(this, "공유할 이미지가 없습니다.", Toast.LENGTH_SHORT).show()
            }

            // 텍스트 + 이미지 공유
            view.findViewById<Button>(R.id.btnShareTextAndImage).setOnClickListener {

                // 두 가지 모두 전송하도록 반드시 이미지가 존재할 경우에만 수행
                if (VM.isExistText() && VM.isExistImage()) {
                    // 이미지 전송
                    ShareImages()
                    // 텍스트 클립보드 복사
                    val clipboardManager =
                        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clipData = ClipData.newPlainText(
                        "MemoText",
                        VM.titleTemp + "\n\n" + VM.contentTemp
                    )
                    clipboardManager.setPrimaryClip(clipData)
                    Toast.makeText(
                        this,
                        "텍스트가 클립보드에 복사되었습니다.\n텍스트를 붙여넣기 해주세요.",
                        Toast.LENGTH_LONG
                    ).show()
                } else Toast.makeText(this, "공유할 텍스트 혹은 이미지가 없습니다.", Toast.LENGTH_SHORT)
                    .show()
            }

            alertDialog
                .setView(view)
                .setTitle("공유")
                .show()
        }
    }
}