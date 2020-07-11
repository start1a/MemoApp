package com.start3a.memoji.viewmodel

import android.content.DialogInterface
import androidx.lifecycle.ViewModel
import com.start3a.memoji.data.Category
import com.start3a.memoji.data.RealmLiveData
import com.start3a.memoji.repository.Repository

class CategoryViewModel: ViewModel() {

    // Repository
    private val repository = Repository()

    // Memo
    var memoID: String? = null

    val categoryLiveData: RealmLiveData<Category> by lazy {
        RealmLiveData(repository.getCategories())
    }
    var dialogInterface: DialogInterface? = null

    fun addCategory(cat: Category) {
        repository.addCategory(cat)
    }

    fun isExistCategory(name: String): Boolean {
        return categoryLiveData.value!!.none { it.nameCat == name }
    }

    override fun onCleared() {
        super.onCleared()
        repository.onCleared()
    }

    fun updateCategory(cat: Category, newName: String) {
        repository.updateCategory(cat, newName)
    }

    fun deleteCategory(cat: Category) {
        repository.deleteCategory(cat)
    }
}