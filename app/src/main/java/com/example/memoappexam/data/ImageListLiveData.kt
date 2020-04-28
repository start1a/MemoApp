package com.example.memoappexam.data

import androidx.lifecycle.LiveData

class ImageListLiveData<T> (private var list: MutableList<T> = mutableListOf())
    : LiveData<MutableList<T>>() {

    init {
        value = list
    }

    fun add(data: T) {
        list.add(data)
        value = list
    }

    fun deleteMultiple(listDelete: List<Int>) {
        for (element in listDelete)
            list.removeAt(element)
        value = list
    }

}