package com.start3a.memoji.data

import androidx.lifecycle.LiveData
import io.realm.RealmList
import io.realm.RealmObject

// DB 메모의 이미지 리스트 출력
class RealmImageFileLiveData<T: RealmObject> (private val realmList: RealmList<T>)
    : LiveData<RealmList<T>>(){

    init {
        value = realmList
    }

    // 이미지를 가져와 리스트에 저장
    fun AddDatas(listAdd: RealmList<T>) {
        val list = value
        list!!.addAll(listAdd)
        value = list
    }
}
