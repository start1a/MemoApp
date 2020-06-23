package com.start3a.memoji.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.start3a.memoji.MemoAlarmTool
import com.start3a.memoji.Model.FireStoreDao
import com.start3a.memoji.Model.MemoDao
import com.start3a.memoji.data.MemoData
import com.start3a.memoji.data.MemoImageFilePath
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmResults
import java.io.File
import java.util.*

class MemoRepository {

    companion object {
        // 유저 아이디
        var userID: String? = FirebaseAuth.getInstance().currentUser?.email

        // 파이어스토어
        private val mFireStoreDao: FireStoreDao by lazy {
            FireStoreDao(FirebaseFirestore.getInstance())
        }
    }

    // 로컬 DB
    private val mRealm: Realm by lazy {
        Realm.getDefaultInstance()
    }
    private val mMemoDao: MemoDao by lazy {
        MemoDao(mRealm)
    }

    // 로그아웃
    fun signOutUser(context: Context) {
        // 등록된 사용자 알람 제거
        val alarms = mMemoDao.getAllMemoAlarms()
        alarms.forEach { memo ->
            memo.alarmTimeList.forEach { time ->
                MemoAlarmTool.deleteAlarm(context, memo.id, time)
            }
        }

        // 캐시 이미지 파일 디렉토리 제거
//        val dir = context.filesDir.toString()
//         val userDataDirList = File(dir).listFiles()

        // 로컬 DB 제거
        mMemoDao.clearDatabase()
    }

    fun getUserData(context: Context, notifyListener: () -> Unit) {
        // 메모 불러오기
        mFireStoreDao.getUserMemoData(context) { list ->
            mMemoDao.SaveFireStoreMemoData(list)
            notifyListener()
        }
    }

    fun getAllMemos(): RealmResults<MemoData> = mMemoDao.getAllMemos()

    fun loadMemo(id: String): MemoData = mMemoDao.getMemoByID(id)

    fun saveMemo(
        memoData: MemoData,
        title: String,
        content: String,
        imageFileLinks: RealmList<MemoImageFilePath>,
        alarmTimeList: RealmList<Date>
    ) {
        mMemoDao.saveMemo(memoData, title, content, imageFileLinks, alarmTimeList)
        mFireStoreDao.saveMemo(memoData)
    }

    fun deleteMemo(id: String, memoDir: String) {
        mMemoDao.deleteMemo(id)
        mFireStoreDao.deleteMemo(id)
        setDirEmpty(memoDir)
    }

    private fun setDirEmpty(dir: String) {
        val dirFile = File(dir)
        val childFileList = dirFile.listFiles()

        if (dirFile.exists()) {
            for (childFile in childFileList) {
                if (childFile.isDirectory) {
                    // 자식 디렉토리로 재귀 호출
                    setDirEmpty(childFile.absolutePath)
                } else {
                    childFile.delete()
                }
            }
            dirFile.delete()
        }
    }

    fun onCleared() {
        mRealm.close()
    }
}