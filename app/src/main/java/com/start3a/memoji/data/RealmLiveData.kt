package com.start3a.memoji.data

import androidx.lifecycle.LiveData
import io.realm.RealmChangeListener
import io.realm.RealmObject
import io.realm.RealmResults

// DB에서 쿼리한 메모 리스트 저장
class RealmLiveData<T : RealmObject>(private val realmResults: RealmResults<T>) :
    LiveData<RealmResults<T>>() {

    init {
        value = realmResults
    }

    private val listener = RealmChangeListener<RealmResults<T>> { value = it }

    override fun onActive() {
        super.onActive()
        realmResults.addChangeListener(listener)
    }

    override fun onInactive() {
        super.onInactive()
        realmResults.removeChangeListener(listener)
    }

}