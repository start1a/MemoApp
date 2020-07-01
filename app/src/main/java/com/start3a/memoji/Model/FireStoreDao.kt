package com.start3a.memoji.Model

import android.content.Context
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.start3a.memoji.MemoAlarmTool
import com.start3a.memoji.data.ImgFilePathForFireStore
import com.start3a.memoji.data.MemoData
import com.start3a.memoji.data.MemoDataForFireStore
import com.start3a.memoji.data.MemoImageFilePath
import com.start3a.memoji.repository.Repository
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Function
import io.reactivex.rxjava3.schedulers.Schedulers
import io.realm.RealmList
import java.util.*

class FireStoreDao(private val mFireStore: FirebaseFirestore) {

    companion object {
        private const val TAG = "FireStoreDao"
    }

    // 사용자 메모 가져오기
    // FireStore -> Realm DB
    fun getUserMemos(context: Context, setUserDatalistener: (MutableList<MemoData>) -> Unit) {
        val list = mutableListOf<MemoData>()

        val completeTaskFunction = Function<QueryDocumentSnapshot, MemoData> { doc ->
            val objFireStore = doc.toObject(MemoDataForFireStore::class.java)
            getMemoFireStoreToRealm(objFireStore)
        }

        val observer = object : Observer<MemoData> {
            override fun onComplete() {
                setUserDatalistener(list)
            }

            override fun onSubscribe(d: Disposable?) {}

            override fun onNext(memo: MemoData?) {
                if (memo != null) {
                    // 알람 리스트 갱신
                    val deleteAlarmIndexList = mutableListOf<Int>()
                    for (index in 0 until memo.alarmTimeList.size) {
                        val time = memo.alarmTimeList[index]!!
                        if (time.after(Date()))
                            MemoAlarmTool.addAlarm(context, memo.id, time)
                        else
                            deleteAlarmIndexList.add(index)
                    }
                    for (i in deleteAlarmIndexList.size - 1 downTo 0)
                        memo.alarmTimeList.removeAt(i)
                    list.add(memo)
                }
            }

            override fun onError(e: Throwable?) {
                e?.printStackTrace()
            }
        }

        setDBAndNotify(Repository.MEMOS, completeTaskFunction, observer)
    }

    // 컬렉션마다 타입을 지정하여 FireStore로부터 데이터 요청
    private fun <R : Any> setDBAndNotify(
        nameCollection: String,
        function: Function<QueryDocumentSnapshot, R>,
        observer: Observer<R>
    ) {
        mFireStore.collection(Repository.USERS).document(Repository.userID!!)
            .collection(nameCollection)
            .get()
            .addOnSuccessListener { documents ->
                Observable
                    .fromIterable(documents)
                    .subscribeOn(Schedulers.io())
                    .map(function)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(observer)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    private fun getMemoFireStoreToRealm(memo: MemoDataForFireStore): MemoData {
        return MemoData(
            memo.id ?: "",
            memo.title ?: "",
            memo.content ?: "",
            memo.summary ?: "",
            memo.date ?: Date(),
            // 이미지
            RealmList<MemoImageFilePath>().apply {
                memo.imageFileLinks?.let { images ->
                    for (imageData in images) {
                        add(
                            MemoImageFilePath(
                                imageData.uri,
                                imageData.thumbnailPath,
                                imageData.originalPath
                            )
                        )
                    }
                }
            },
            // 알람
            RealmList<Date>().apply {
                memo.alarmTimeList?.let { alarms ->
                    for (alarmData in alarms) {
                        add(alarmData.toDate())
                    }
                }
            }
        )
    }

    fun saveMemo(memo: MemoData) {
        val docMemoData = hashMapOf(
            "id" to memo.id,
            "title" to memo.title,
            "content" to memo.content,
            "summary" to memo.summary,
            "date" to Timestamp(memo.date),
            "imageFileLinks" to arrayListOf<ImgFilePathForFireStore>().apply {
                memo.imageFileLinks.forEach {
                    add(
                        ImgFilePathForFireStore(
                            it.uri,
                            it.thumbnailPath,
                            it.originalPath
                        )
                    )
                }
            },
            "alarmTimeList" to arrayListOf<Timestamp>().apply {
                memo.alarmTimeList.forEach {
                    add(Timestamp(it))
                }
            }
        )

        mFireStore.collection(Repository.USERS).document(Repository.userID!!)
            .collection(Repository.MEMOS).document(memo.id)
            .set(docMemoData)
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
    }

    fun deleteMemo(memoID: String) {
        mFireStore.collection(Repository.USERS).document(Repository.userID!!)
            .collection(Repository.MEMOS).document(memoID)
            .delete()
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
    }
}