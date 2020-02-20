package com.example.memoappexam.data

import androidx.lifecycle.LiveData
import io.realm.RealmChangeListener
import io.realm.RealmList
import io.realm.RealmObject

class RealmListLiveData<T: RealmObject> (private val realmList: RealmList<T>)
    : LiveData<RealmList<T>>(){

    init {
        value = realmList
    }

    fun add(data: T) {
        realmList.add(data)
        value = realmList
    }

    fun remove(index: Int) {
        realmList.removeAt(index)
        value = realmList
    }
}
