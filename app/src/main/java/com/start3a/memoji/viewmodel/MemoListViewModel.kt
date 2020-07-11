package com.start3a.memoji.viewmodel

import android.content.Context
import android.view.Menu
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.start3a.memoji.data.Category
import com.start3a.memoji.data.MemoData
import com.start3a.memoji.data.RealmLiveData
import com.start3a.memoji.repository.Repository

class MemoListViewModel : ViewModel() {

    // Repository
    private val repository = Repository()

    // UI
    var mActionMenu: Menu? = null
    var layoutMemoList = MutableLiveData<Int>().apply { value = LAYOUT_LINEAR }
    var curTab = MutableLiveData<String>().apply { value = "모든 메모" }
    lateinit var navMenu: Menu

    // 리스트
    var memoListLiveData: RealmLiveData<MemoData> = RealmLiveData(repository.getAllCatMemos(curTab.value!!))
    val categoryLiveData: RealmLiveData<Category> by lazy {
        RealmLiveData(repository.getCategories())
    }
    var listNotifyListener: (() -> Unit)? = null
    var newMemoListQueryListener: ((MutableList<MemoData>) -> Unit)? = null

    // 상수
    val LAYOUT_LINEAR = 0
    val LAYOUT_GRID = 1

    // Context
    lateinit var context: Context

    // 로그인
    var isSingingIn = false

    fun setLayoutMemoList(type: Int) {
        layoutMemoList.value = type
    }

    fun setNewListQuery() {
        memoListLiveData = RealmLiveData(repository.getAllCatMemos(curTab.value!!))
        newMemoListQueryListener?.let { it(memoListLiveData.value!!) }
    }

    // 로그인 시 사용자 데이터 불러오기
    fun getUserData() {
        repository.getUserData(context, listNotifyListener!!)
    }

    fun signOutUser() {
        repository.signOutUser(context)
    }

    fun setCurTab(name: String) {
        curTab.value = name
    }

    override fun onCleared() {
        super.onCleared()
        repository.onCleared()
    }
}