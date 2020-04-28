package com.example.memoappexam.data

import androidx.lifecycle.LiveData
import io.realm.RealmChangeListener
import io.realm.RealmList
import io.realm.RealmObject

class RealmImageFileLiveData<T: RealmObject> (private val realmList: RealmList<T>)
    : LiveData<RealmList<T>>(){

    init {
        value = realmList
    }

    fun AddDatas(listAdd: RealmList<T>) {
        val list = value
        list!!.addAll(listAdd)
        value = list
    }

//    private val listener = RealmChangeListener<RealmList<T>> { value = it }

//    override fun onActive() {
//        super.onActive()
//        if (realmList.isManaged) realmList.addChangeListener(listener)
//    }
//
//    override fun onInactive() {
//        super.onInactive()
//        if (realmList.isManaged) realmList.removeChangeListener(listener)
//    }
}
