package com.start3a.memoji.Model

import android.content.Context
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.start3a.memoji.MemoAlarmTool
import com.start3a.memoji.data.*
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
            val objFireStore = doc.toObject(MemoDataFS::class.java)
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

    fun getUserCategories(setToRealmListener: (MutableList<Category>) -> Unit) {
        mFireStore.collection(Repository.USERS).document(Repository.userID!!)
            .collection(Repository.CATEGORIES)
            .get()
            .addOnSuccessListener { docs ->
                val list = mutableListOf<Category>()
                docs.forEach { doc ->
                    val cat = doc.toObject(CategoryFS::class.java).run {
                        Category(id.toLong(), nameCat)
                    }
                    list.add(cat)
                }
                setToRealmListener(list)
            }
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
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(function)
                    .subscribe(observer)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    private fun getMemoFireStoreToRealm(memo: MemoDataFS): MemoData {
        return MemoData(
            memo.id,
            memo.title,
            memo.content,
            memo.summary,
            memo.date,
            // 이미지
            RealmList<MemoImageFilePath>().apply {
                memo.imageFileLinks.let { images ->
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
                memo.alarmTimeList.let { alarms ->
                    for (alarmData in alarms) {
                        add(alarmData.toDate())
                    }
                }
            },
            memo.category
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

        val doc = mFireStore.collection(Repository.USERS).document(Repository.userID!!)
            .collection(Repository.MEMOS).document(memo.id)
        doc.set(docMemoData)
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
        doc.update("category", memo.category)
    }

    fun deleteMemo(memoID: String) {
        mFireStore.collection(Repository.USERS).document(Repository.userID!!)
            .collection(Repository.MEMOS).document(memoID)
            .delete()
            .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
    }

    fun addCategory(cat: Category) {
        val docCat = hashMapOf(
            "id" to cat.id.toString(),
            "nameCat" to cat.nameCat
        )

        mFireStore.collection(Repository.USERS).document(Repository.userID!!)
            .collection(Repository.CATEGORIES).document(cat.id.toString())
            .set(docCat)
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
    }

    fun updateCategory(catID: Long, newName: String) {
        mFireStore.collection(Repository.USERS).document(Repository.userID!!)
            .collection(Repository.CATEGORIES).document(catID.toString())
            .update("nameCat", newName)
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
    }

    fun deleteCategory(catID: Long) {
        mFireStore.collection(Repository.USERS).document(Repository.userID!!)
            .collection(Repository.CATEGORIES).document(catID.toString())
            .delete()
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
    }

    fun updateCatOfMemo(prevName: String, newName: String) {
        val userRef = mFireStore.collection(Repository.USERS).document(Repository.userID!!)
            .collection(Repository.MEMOS)

        userRef.whereEqualTo("category", prevName)
            .get()
            .addOnSuccessListener { docs ->
                docs.forEach {
                    it.reference.update("category", newName)
                }
            }
            .addOnFailureListener {
                Log.w(TAG, "update CatOfMemo failed")
            }
    }

    fun deleteCatOfMemo(nameCat: String) {
        val userRef = mFireStore.collection(Repository.USERS).document(Repository.userID!!)
            .collection(Repository.MEMOS)

        userRef.whereEqualTo("category", nameCat)
            .get()
            .addOnSuccessListener {
                it.forEach {
                    it.reference.update("category", "")
                }
            }
            .addOnFailureListener {
                Log.w(TAG, "delete CatOfMemo failed")
            }
    }
}