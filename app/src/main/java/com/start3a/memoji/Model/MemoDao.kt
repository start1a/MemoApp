package com.start3a.memoji.Model

import com.start3a.memoji.data.Category
import com.start3a.memoji.data.MemoData
import com.start3a.memoji.data.MemoImageFilePath
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmResults
import io.realm.Sort
import java.util.*

// DB에서 쿼리할 함수 모음
class MemoDao(private val realm: Realm) {

    fun getAllMemos(): RealmResults<MemoData> {
        return realm.where(MemoData::class.java)
            .sort("date", Sort.DESCENDING)
            .findAll()
    }

    fun getCategories(): RealmResults<Category> {
        return realm.where(Category::class.java)
            .sort("nameCat", Sort.ASCENDING)
            .findAll()
    }

    fun getMemoByID(id: String): MemoData {
        val data = realm.where(MemoData::class.java)
            .equalTo("id", id)
            .findFirst() as MemoData
        return realm.copyFromRealm(data)
    }

    fun saveMemo(
        memoData: MemoData,
        title: String,
        content: String,
        imageFileLinks: RealmList<MemoImageFilePath>,
        alarmTimeList: RealmList<Date>
    ) {
        realm.executeTransaction {
            memoData.title = title
            memoData.content = content
            memoData.date = Date()
            memoData.alarmTimeList = alarmTimeList

            if (content.length > 100)
                memoData.summary = "${content.substring(0..100)}.."
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

    fun deleteAlarmMemo(id: String, alarmTime: Date) {
        realm.executeTransaction {
            val memoData = it.where(MemoData::class.java)
                .equalTo("id", id)
                .findFirst()
            for (time in memoData!!.alarmTimeList) {
                if (time == alarmTime) {
                    memoData.alarmTimeList.remove(time)
                    break
                }
            }
        }
    }

    fun getAllMemoAlarms(): MutableList<MemoData> {
        return mutableListOf<MemoData>().apply {
            for (memo in getAllMemos()) {
                if (memo.alarmTimeList.size > 0)
                    add(memo)
            }
        }
    }


    fun SaveFireStoreMemoData(memos: MutableList<MemoData>) {
        realm.executeTransaction {
            for (memo in memos) {
                it.copyToRealmOrUpdate(memo)
            }
        }
    }

    fun clearDatabase() {
        // 로컬 DB 데이터 비우기
        realm.executeTransaction {
            getAllMemos().deleteAllFromRealm()
            getCategories().deleteAllFromRealm()
        }
    }

    fun getAllCatMemos(nameCat: String): RealmResults<MemoData> {
        return realm.where(MemoData::class.java)
            .equalTo("category", nameCat)
            .sort("date", Sort.DESCENDING)
            .findAll()
    }

    fun addCategory(cat: Category) {
        realm.executeTransaction {
            it.copyToRealmOrUpdate(cat)
        }
    }

    fun updateCategory(id: Long, newName: String) {
        realm.executeTransaction {
            val cat = it.where(Category::class.java)
                .equalTo("id", id)
                .findFirst()
            cat?.nameCat = newName
            it.copyToRealmOrUpdate(cat)
        }
    }

    fun deleteCategory(id: Long) {
        realm.executeTransaction {
            it.where(Category::class.java)
                .equalTo("id", id)
                .findFirst()?.deleteFromRealm()
        }
    }

    fun updateCatOfMemo(prevName: String, newName: String) {
        realm.executeTransaction {
            val list = realm.where(MemoData::class.java)
                .equalTo("category", prevName)
                .findAll()
            list.forEach {
                it.category = newName
            }
            it.copyToRealmOrUpdate(list)
        }
    }

    fun deleteCatOfMemo(nameCat: String) {
        realm.executeTransaction {
            val list = realm.where(MemoData::class.java)
                .equalTo("category", nameCat)
                .findAll()
            list.deleteAllFromRealm()
        }
    }
}