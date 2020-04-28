package com.example.memoappexam.Model

import com.example.memoappexam.data.MemoData
import com.example.memoappexam.data.MemoImageFilePath
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmResults
import io.realm.Sort
import java.io.File
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

    fun addUpdateMemo(
        memoData: MemoData,
        title: String,
        content: String,
        imageFileLinks: RealmList<MemoImageFilePath>
    ) {
        realm.executeTransaction {
            memoData.title = title
            memoData.content = content
            memoData.date = Date()

            if (content.length > 100)
                memoData.summary = content.substring(0..150) + ".."
            else
                memoData.summary = content

            if (!memoData.isManaged) {
                memoData.imageFileLinks = imageFileLinks
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

    fun Add_NewImage(realmList: RealmList<MemoImageFilePath>, list: List<MemoImageFilePath>) {
        realm.executeTransaction {
            realmList.addAll(list)
        }
    }

    fun CopyImageFileFromUri(realmList: RealmList<MemoImageFilePath>, index: Int, images: MemoImageFilePath) {
        realm.executeTransaction {
            realmList[index]?.thumbnailPath = images.thumbnailPath
            realmList[index]?.originalPath = images.originalPath
        }
    }

    fun Delete_OneImage(realmList: RealmList<MemoImageFilePath>, index: Int) {
        realm.executeTransaction {
            realmList.removeAt(index)
        }
    }

//    fun Delete_SelectedImage(realmList: RealmList<MemoImageFilePath>, list: List<Int>) {
//            realm.executeTransaction {
//                for (element in list) {
//                    realmList.removeAt(element)
//                }
//        }
//    }
}