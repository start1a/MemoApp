package com.start3a.memoji.viewmodel

import android.view.Menu
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.start3a.memoji.Model.MemoDao
import com.start3a.memoji.data.MemoData
import com.start3a.memoji.data.RealmLiveData
import io.realm.Realm

class MemoListViewModel : ViewModel() {

    var mMenu: Menu? = null

    var layout_memoList: MutableLiveData<Int> = MutableLiveData<Int>().apply { value = LAYOUT_LINEAR }

    val LAYOUT_LINEAR = 0
    val LAYOUT_GRID = 1

    private val mRealm: Realm by lazy {
        Realm.getDefaultInstance()
    }

    private val mMemoDao: MemoDao by lazy {
        MemoDao(mRealm)
    }

    val memoListLiveData: RealmLiveData<MemoData> by lazy {
        RealmLiveData<MemoData>(mMemoDao.getAllMemos())
    }

    fun setLayoutMemoList(type: Int) {
        layout_memoList.value = type
    }

    override fun onCleared() {
        super.onCleared()
        mRealm.close()
    }
}