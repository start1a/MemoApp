package com.example.memoappexam.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.memoappexam.data.MemoDao
import com.example.memoappexam.data.MemoData
import com.example.memoappexam.data.RealmLiveData
import io.realm.Realm

class MemoListViewModel: ViewModel() {

    private val mRealm: Realm by lazy {
        Realm.getDefaultInstance()
    }

    private val mMemoDao: MemoDao by lazy {
        MemoDao(mRealm)
    }

    val memoListLiveData: RealmLiveData<MemoData> by lazy {
        RealmLiveData<MemoData>(mMemoDao.getAllMemos())
    }

    override fun onCleared() {
        super.onCleared()
        mRealm.close()
    }
}