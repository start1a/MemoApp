package com.start3a.memoji.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.start3a.memoji.MemoAlarmTool
import com.start3a.memoji.Model.CloudStorageDao
import com.start3a.memoji.Model.FireStoreDao
import com.start3a.memoji.Model.MemoDao
import com.start3a.memoji.data.MemoData
import com.start3a.memoji.data.MemoImageFilePath
import com.start3a.memoji.views.LoadingProgressBar
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmResults
import java.io.File
import java.util.*

class Repository {

    companion object {
        // 유저 아이디
        const val USERS = "Users"
        var userID: String? = FirebaseAuth.getInstance().currentUser?.email
        const val FILESDIR = "data/user/0/com.start3a.memoji/files"
        const val MEMOS = "Memos"
        const val THUMBNAIL_PATH = "ImageThumbnail"
        const val ORIGINAL_PATH = "ImageOriginal"
    }
    // 로컬 DB
    private val mRealm: Realm by lazy {
        Realm.getDefaultInstance()
    }
    private val mRealmDao: MemoDao by lazy {
        MemoDao(mRealm)
    }

    // 파이어스토어
    private val mFireStoreDao: FireStoreDao by lazy {
        FireStoreDao(FirebaseFirestore.getInstance())
    }

    // 스토리지
    private val mStorageRef: StorageReference by lazy {
        FirebaseStorage.getInstance().reference
    }

    private val mStorageDao: CloudStorageDao by lazy {
        CloudStorageDao(mStorageRef)
    }

    // 로그아웃
    fun signOutUser(context: Context) {
        // 등록된 사용자 알람 제거
        val alarms = mRealmDao.getAllMemoAlarms()
        alarms.forEach { memo ->
            memo.alarmTimeList.forEach { time ->
                MemoAlarmTool.deleteAlarm(context, memo.id, time)
            }
        }

        // 캐시 이미지 파일 디렉토리 제거
        setDirEmpty("${context.filesDir}/${MEMOS}")

        // 로컬 DB 제거
        mRealmDao.clearDatabase()
    }

    fun getUserData(context: Context, notifyListener: () -> Unit) {
        // 메모 불러오기
        mFireStoreDao.getUserMemos(context) { list ->
            mRealmDao.SaveFireStoreMemoData(list)
            notifyListener()
            LoadingProgressBar.dialogInterfaceLoading?.dismiss()
        }
        // Storage: 모든 메모 이미지 파일
        val dirMemos = "$FILESDIR/${MEMOS}"
        mStorageDao.getImageFile("$USERS/$userID/$dirMemos", dirMemos, notifyListener)
    }

    fun getAllMemos(): RealmResults<MemoData> = mRealmDao.getAllMemos()

    fun loadMemo(id: String): MemoData = mRealmDao.getMemoByID(id)

    fun saveMemo(
        memoData: MemoData,
        title: String,
        content: String,
        imageFileLinks: RealmList<MemoImageFilePath>,
        listImageForStorage: MutableList<MemoImageFilePath>,
        alarmTimeList: RealmList<Date>
    ) {
        mRealmDao.saveMemo(memoData, title, content, imageFileLinks, alarmTimeList)
        mFireStoreDao.saveMemo(memoData)

        if (listImageForStorage.size > 0)
            mStorageDao.saveImageFileOfMemo("$USERS/$userID", listImageForStorage)
    }

    fun deleteMemo(id: String, memoDir: String) {
        mRealmDao.deleteMemo(id)
        mFireStoreDao.deleteMemo(id)
        val pathMemo = "$USERS/$userID/$memoDir"
        mStorageDao.deleteAllImageOfMemo(pathMemo)
        setDirEmpty(memoDir)
    }

    fun deleteImageOfMemo(deleteList: MutableList<MemoImageFilePath>) {
        mStorageDao.deleteImageOfMemo("$USERS/$userID", deleteList)
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