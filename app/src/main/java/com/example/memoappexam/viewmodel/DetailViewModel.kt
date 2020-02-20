package com.example.memoappexam.viewmodel

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

    val title: MutableLiveData<String> = MutableLiveData<String>().apply { value = "" }
    val content: MutableLiveData<String> = MutableLiveData<String>().apply { value = "" }
    val image: RealmListLiveData<MemoImageData> by lazy {
        RealmListLiveData<MemoImageData>(memoData.images)
    }
    var memoId: String? = null
    private var memoData = MemoData()
    var fragBtnClicked: Int = R.id.btnFragText

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

    fun saveLiveData(editTitle: String, editContent: String) {
        title.value = editTitle
        content.value = editContent
    }

    fun Load_MemoData(id: String) {
        memoData = mMemoDao.selectMemo(id)
        memoId = id
        title.value = memoData.title
        content.value = memoData.content
    }

    fun Update_MemoData() {
        if (title.value?.count() ?: 0 > 0 || content.value?.count() ?: 0 > 0 || image.value?.size ?: 0 > 0)
            mMemoDao.addUpdateMemo(
                memoData, title.value ?: "", content.value ?: "", image.value ?: RealmList()
            )
    }

    fun Delete_MemoData(id: String) {
        mMemoDao.deleteMemo(id)
    }

    fun add_ImageMemoData(imageStr: String) {
        image.add(MemoImageData(imageStr))
    }
}