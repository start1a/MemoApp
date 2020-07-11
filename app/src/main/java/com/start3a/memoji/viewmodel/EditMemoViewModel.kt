package com.start3a.memoji.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.view.Menu
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.start3a.memoji.ImageCreateManager
import com.start3a.memoji.MemoAlarmTool
import com.start3a.memoji.R
import com.start3a.memoji.data.MemoData
import com.start3a.memoji.data.MemoImageFilePath
import com.start3a.memoji.data.RealmImageFileLiveData
import com.start3a.memoji.repository.Repository
import io.realm.RealmList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class EditMemoViewModel : ViewModel() {

    // 텍스트
    val title = MutableLiveData<String>()
    val content = MutableLiveData<String>()
    var titleTemp: String = ""
    var contentTemp: String = ""

    // 이미지
    val imageFileLinks: RealmImageFileLiveData<MemoImageFilePath> by lazy {
        RealmImageFileLiveData(memoData.imageFileLinks)
    }
    lateinit var deleteImageListListener: () -> Unit

    // 메모 저장 시 이미지 파일 삭제 반영
    private val imageFileDeleteList: MutableList<MemoImageFilePath> by lazy {
        mutableListOf<MemoImageFilePath>()
    }

    // 알람
    // 현재 알람 리스트
    val alarmTimeList: MutableLiveData<RealmList<Date>> =
        MutableLiveData<RealmList<Date>>().apply { value = RealmList() }

    // 초기 등록 알람 리스트
    // 시스템에 등록된 알람들. 알람 갱신 비교용
    val alarmTimeListTemp: MutableList<Date> = mutableListOf()
    lateinit var datePickerShowListener: (index: Int) -> Unit
    lateinit var deleteAlarmListListener: () -> Unit

    // 카테고리
    var category = MutableLiveData<String>()

    // 코루틴
    private val MAX_COROUTINE_JOB = 4
    private var mJob = Job()
    private val backScope = CoroutineScope(Dispatchers.Default + mJob)

    // Context
    lateinit var context: Context

    // 임시 메모 정보
    // 새 메모 = null 기존 메모 = !null
    var memoId: String? = null
    var memoData = MemoData()

    // UI 정보
    var mMenu: Menu? = null
    var editable = MutableLiveData<Boolean>()
    var fragBtnClicked: MutableLiveData<Int> =
        MutableLiveData<Int>().apply { value = R.id.action_fragment_text }

    // 데이터 삭제 인덱스 리스트
    // 화면 갱신 시 데이터 유지
    var deleteDataList: MutableList<Int> = mutableListOf()

    // 해당 탭의 컨텐츠 삭제 메뉴 리스너
    lateinit var allContentSelectListener: () -> Unit

    // repository
    private val repository = Repository()

    override fun onCleared() {
        super.onCleared()
        mJob.cancel()
        repository.onCleared()
    }

    fun saveText() {
        title.value = titleTemp
        content.value = contentTemp
    }

    fun setEditable(on: Boolean) {
        editable.value = on
    }

    fun setFragBtn(type: Int) {
        fragBtnClicked.value = type
    }

    fun isExistText(): Boolean {
        return titleTemp.isNotEmpty() || contentTemp.isNotEmpty()
    }

    fun isExistImage(): Boolean {
        return imageFileLinks.value?.size ?: 0 > 0
    }

    // 메모 불러오기
    fun Load_MemoData(id: String) {
        memoData = repository.loadMemo(id)
        memoId = id
        titleTemp = memoData.title
        contentTemp = memoData.content
        alarmTimeList.value = memoData.alarmTimeList
        alarmTimeListTemp.addAll(memoData.alarmTimeList)
        category.value = memoData.category
    }

    // 메모 수정
    suspend fun AddOrUpdate_MemoData() {
        imageFileLinks.value?.let { images ->
            // 내용이 존재할 경우
            if (titleTemp.isNotEmpty() || contentTemp.isNotEmpty() || images.size > 0) {
                val alarmTimeListValue = alarmTimeList.value!!

                // 새 메모일 경우
                // 해당 메모 이미지 디렉토리 생성
                if (memoId.isNullOrEmpty()) {
                    ImageCreateManager.setImageDirectory(context.filesDir, memoData.id)
                }

                // 파일이 있는 이미지 삭제 리스트
                if (imageFileDeleteList.size > 0) {
                    repository.deleteImageOfMemo(imageFileDeleteList)
                    for (image in imageFileDeleteList) {
                        File(image.thumbnailPath).delete()
                        File(image.originalPath).delete()
                    }
                }

                val listImageForStorage = mutableListOf<MemoImageFilePath>()
                backScope.launch {

                    var num = 0
                    val max = images.size.run {
                        if (this < MAX_COROUTINE_JOB)
                            this
                        else MAX_COROUTINE_JOB
                    }
                    var index = images.size - 1
                    while (num++ < max) {
                        launch {
                            // 거꾸로 이미지 리스트 탐색
                            while (index >= 0) {
                                // 파일이 존재하지 않는 아이템만 탐색 / 파일 저장
                                if (index-- >= 0 && images[index + 1]!!.thumbnailPath.isEmpty()) {
                                    val item = images[index + 1]!!
                                    val fileLinks = GetMemoImageFilePath(
                                        item.uri,
                                        "${context.filesDir}/${Repository.MEMOS}/${memoData.id}"
                                    )
                                    item.thumbnailPath = fileLinks.thumbnailPath
                                    item.originalPath = fileLinks.originalPath
                                    listImageForStorage.add(fileLinks)
                                } else break
                            }
                        }
                    }
                }.join()

                // 기존 알람 삭제
                alarmTimeListTemp.forEach {
                    // 이전 시간 알람 OR 사용자가 삭제한 알람
                    if (it.before(Date()) || !alarmTimeListValue.contains(it))
                        MemoAlarmTool.deleteAlarm(context, memoData.id, it)
                }
                // 새로 알람 갱신
                val deleteIndexList = mutableListOf<Int>()
                for (i in 0 until alarmTimeListValue.size) {
                    val time = alarmTimeListValue[i]!!
                    // 현재 이후 알람 AND 신규 생성 알람
                    if (time.after(Date()) && !alarmTimeListTemp.contains(time)) {
                        MemoAlarmTool.addAlarm(context, memoData.id, time)
                    }
                    // 시간이 지난 알람은 제거 리스트에 추가됨
                    // 나중에 삭제하는 이유 : ConcurrentModificationException
                    // 어떤 스레드가 Iterator가 반복중인 Collection을 수정하려 할 때 발생
                    else {
                        deleteIndexList.add(i)
                    }
                }
                // 지난 알람 삭제
                for (i in 0 until deleteIndexList.size)
                    alarmTimeListValue.removeAt(i)

                // 카테고리
                memoData.category = category.value!!

                //  메모를 DB에 저장
                repository.saveMemo(
                    memoData,
                    titleTemp,
                    contentTemp,
                    images,
                    listImageForStorage,
                    alarmTimeListValue
                )
            }
            // 내용이 없는 기존 메모이면 DB + 디렉토리 삭제
            else Delete_MemoData()
        }
    }

    private fun GetMemoImageFilePath(imgSrc: String, memoDir: String): MemoImageFilePath {
        var bitmapOriginal: Bitmap? = null
        var bitmapThumbnail: Bitmap? = null

        if (imgSrc.startsWith("content://")) {
            bitmapOriginal = ImageCreateManager.getURIToBitmap(context, imgSrc)
            bitmapThumbnail = ImageCreateManager.getURIToBitmapResize(context, imgSrc)
        }
        else if (imgSrc.startsWith("http")) {
            bitmapOriginal = ImageCreateManager.getURLToBitmap(imgSrc)
            bitmapThumbnail = ImageCreateManager.getURLToBitmapResize(imgSrc)
        }
        // 비트맵이 생성됨
        if (bitmapOriginal != null && bitmapThumbnail != null) {
            val pathOriginal = ImageCreateManager.saveBitmapToJpeg(
                bitmapOriginal,
                "$memoDir/${Repository.ORIGINAL_PATH}"
            )
            val pathThumbnail = ImageCreateManager.saveBitmapToJpeg(
                bitmapThumbnail,
                "$memoDir/${Repository.THUMBNAIL_PATH}"
            )

            bitmapOriginal.recycle()
            bitmapThumbnail.recycle()
            // 파일이 생성됨
            if (pathOriginal.isNotEmpty() && pathThumbnail.isNotEmpty()) {
                return MemoImageFilePath(thumbnailPath = pathThumbnail, originalPath = pathOriginal)
            }
            // 적어도 둘 중 하나의 파일이 생성되지 않음
            else {
                // 존재하는 파일은 제거
                File(pathOriginal).delete()
                File(pathThumbnail).delete()
            }
        }
        return MemoImageFilePath(
            thumbnailPath = ImageCreateManager.UNSAVABLE_IMAGE,
            originalPath = ImageCreateManager.UNSAVABLE_IMAGE
        )
    }

    fun Delete_MemoData() {
        // 기존 메모
        if (!memoId.isNullOrEmpty()) {
            // 설정된 알람 삭제
            // alarmTimeListTemp : 메모 초기 알람 상태 보유
            // 아직 저장하지 않았으므로 초기 알람만 설정되어 있음
            for (alarmTime in alarmTimeListTemp)
                MemoAlarmTool.deleteAlarm(context, memoData.id, alarmTime)
            // 데이터 제거 (DB, 디렉토리)
            val memoDir = "${context.filesDir}/${Repository.MEMOS}/$memoId"
            repository.deleteMemo(memoData.id, memoDir)
        }
    }

    // 이미지 추가
    fun addImageList(listAdd: List<String>) {
        // uri 형태로 출력
        val list = RealmList<MemoImageFilePath>()
        listAdd.forEach { list.add(MemoImageFilePath(it)) }
        imageFileLinks.AddDatas(list)
    }

    // 이미지 삭제
    fun DeleteImageList(list: List<Int>) {
        // 내림차순 정렬 후 인덱스 별 삭제
        Collections.sort(list, Collections.reverseOrder())

        imageFileLinks.value?.let { files ->
            for (i in list) {
                // 파일이 존재하는 이미지
                if (files[i]!!.thumbnailPath.isNotEmpty() &&
                    files[i]!!.thumbnailPath != ImageCreateManager.UNSAVABLE_IMAGE
                ) {
                    // 파일 삭제 리스트에 추가
                    // 나중에 메모 갱신 시 사용됨
                    imageFileDeleteList.add(files[i]!!)
                }
                files.removeAt(i)
            }
        }
    }

    fun SetAlarm(time: Date): Boolean {
        // 동일 알람이 존재하지 않아야 생성됨
        val isSetable = alarmTimeList.value!!.none { it == time }
        if (isSetable) {
            val list = alarmTimeList.value!!.apply {
                add(time)
                sort()
            }
            alarmTimeList.value = list
        }
        return isSetable
    }

    fun UpdateAlarm(index: Int, time: Date): Boolean {
        // 동일 알람이 존재하지 않아야 수정됨
        val isUpdatable = alarmTimeList.value!!.none { it == time }
        if (isUpdatable) {
            alarmTimeList.value!![index] = time
            alarmTimeList.value!!.sort()
        }
        return isUpdatable
    }

    fun DeleteAlarm(list: List<Int>) {
        // 내림차순 정렬
        Collections.sort(list, Collections.reverseOrder())
        val deleteList = alarmTimeList.value!!
        for (i in list) {
            deleteList.removeAt(i)
        }
        alarmTimeList.value = deleteList
    }

    fun setCategory(nameCat: String) {
        category.value = nameCat
    }
}