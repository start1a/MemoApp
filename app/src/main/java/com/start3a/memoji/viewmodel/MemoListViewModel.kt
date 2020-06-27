package com.start3a.memoji.viewmodel

import android.content.Context
import android.view.Menu
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.start3a.memoji.data.MemoData
import com.start3a.memoji.data.RealmLiveData
import com.start3a.memoji.repository.Repository

class MemoListViewModel : ViewModel() {

    // UI
    var mMenu: Menu? = null
    var layout_memoList: MutableLiveData<Int> = MutableLiveData<Int>().apply { value = LAYOUT_LINEAR }
    // 리스트 갱신 리스너
    var listNotifyListener : (() -> Unit)? = null

    // 상수
    val LAYOUT_LINEAR = 0
    val LAYOUT_GRID = 1

    // Context
    lateinit var context: Context

    // 로그인
    var isSingingIn = false

    // Repository
    private val repository = Repository()

    val memoListLiveData: RealmLiveData<MemoData> by lazy {
        RealmLiveData(repository.getAllMemos())
    }

    fun setLayoutMemoList(type: Int) {
        layout_memoList.value = type
    }

    // 로그인 시 사용자 데이터 불러오기
    fun getUserData() {
        repository.getUserData(context, listNotifyListener!!)
    }

    fun signOutUser() {
        repository.signOutUser(context)
    }

    override fun onCleared() {
        super.onCleared()
        repository.onCleared()
    }
}