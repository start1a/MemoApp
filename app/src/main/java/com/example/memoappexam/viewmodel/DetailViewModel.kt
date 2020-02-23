package com.example.memoappexam.viewmodel

import android.view.Menu
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.memoappexam.R
import com.example.memoappexam.data.MemoDao
import com.example.memoappexam.data.MemoData
import com.example.memoappexam.data.MemoImageData
import com.example.memoappexam.data.RealmListLiveData
import com.example.memoappexam.views.MemoTextFragment
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmList
import kotlinx.android.synthetic.main.activity_edit_memo.view.*

class DetailViewModel : ViewModel() {

    // 메모 정보
    val title: MutableLiveData<String> = MutableLiveData<String>()
    val content: MutableLiveData<String> = MutableLiveData<String>()
    val image: RealmListLiveData<MemoImageData> by lazy {
        RealmListLiveData<MemoImageData>(memoData.images)
    }
    var memoId: String? = null
    private var memoData = MemoData()
    // 텍스트 임시 데이터
    var titleTemp: String = ""
    var contentTemp: String = ""

    // UI 정보
    var mMenu: Menu? = null
    var editMode: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { value = false }
    var fragBtnClicked: Int = R.id.btnFragText

    // 메모 텍스트 저장 리스너
    lateinit var memoTitleSaveListener: () -> Unit
    lateinit var memoContentSaveListener: () -> Unit

    private val mRealm: Realm by lazy {
        Realm.getDefaultInstance()
    }

    private val mMemoDao: MemoDao by lazy {
        MemoDao(mRealm)
    }

    override fun onCleared() {
        super.onCleared()
        mRealm.close()
    }

    fun saveLiveData() {
        title.value = titleTemp
        content.value = contentTemp
    }

    fun Load_MemoData(id: String) {
        memoData = mMemoDao.selectMemo(id)
        memoId = id
        titleTemp = memoData.title
        contentTemp = memoData.content
    }

    fun Update_MemoData() {
        mMemoDao.addUpdateMemo(
            memoData, titleTemp, contentTemp, image.value ?: RealmList()
        )
    }

    fun Delete_MemoData(id: String) {
        mMemoDao.deleteMemo(id)
    }

    fun add_ImageMemoDataList(imageStr: String) {
        image.add(MemoImageData(imageStr))
    }
}