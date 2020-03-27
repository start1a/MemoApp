package com.example.memoappexam.Model

import com.example.memoappexam.data.MemoData
import com.example.memoappexam.data.MemoImageData
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmResults
import io.realm.Sort
import java.util.*

class MemoDao(private val realm: Realm) {

    fun getAllMemos(): RealmResults<MemoData> {
        return realm.where(MemoData::class.java)
            .sort("date", Sort.DESCENDING)
            .findAll()
    }

    fun selectMemo(id: String): MemoData {
        val data = realm.where(MemoData::class.java)
            .equalTo("id", id)
            .findFirst() as MemoData
        return realm.copyFromRealm(data)
    }

    fun addUpdateMemo(memoData: MemoData, title: String, content: String, images: RealmList<MemoImageData>) {
        realm.executeTransaction {
            memoData.title = title
            memoData.content = content
            memoData.date = Date()
            memoData.images = images

            if (content.length > 100)
                memoData.summary = content.substring(0..100)
            else
                memoData.summary = content

            if (!memoData.isManaged) {
                it.copyToRealmOrUpdate(memoData)
            }
        }
    }

    fun deleteMemo(id: String) {
        realm.executeTransaction {
            it.where(MemoData::class.java)
                .equalTo("id", id)
                .findFirst()
                ?.deleteFromRealm()
        }
    }
}