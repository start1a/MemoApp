package com.example.memoappexam.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.memoappexam.data.MemoDao
import com.example.memoappexam.data.MemoData
import io.realm.Realm
import io.realm.RealmList

class DetailViewModel: ViewModel() {

    val title: MutableLiveData<String> = MutableLiveData<String>().apply { value = "" }
    val content: MutableLiveData<String> = MutableLiveData<String>().apply { value = "" }
    val image: MutableLiveData<List<String>> = MutableLiveData<List<String>>().apply { value = mutableListOf() }

    private var memoData = MemoData()

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

    fun Load_MemoData(id: String) {
        memoData = mMemoDao.selectMemo(id)
        title.value = memoData.title
        content.value = memoData.content
        image.value = memoData.images
    }

    fun Update_MemoData(title: String, content: String, images: RealmList<String>) {
        mMemoDao.addUpdateMemo(memoData, title, content, images)
    }
}