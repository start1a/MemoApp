package com.start3a.memoji.data

import android.os.Parcel
import android.os.Parcelable
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

// 메모 정보 데이터 클래스
open class MemoData(
    @PrimaryKey
    var id: String = UUID.randomUUID().toString(),
    var title: String = "",
    var content: String = "",
    var summary: String = "",
    var date: Date = Date(),
    var imageFileLinks: RealmList<MemoImageFilePath> = RealmList(),
    var alarmTimeList: RealmList<Date> = RealmList(),
    var category: String = ""
) : RealmObject()

open class Category(
    @PrimaryKey
    var id: Long = UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE,
    var nameCat: String = ""
) : RealmObject(), Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(nameCat)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Category> {
        override fun createFromParcel(parcel: Parcel): Category {
            return Category(parcel)
        }

        override fun newArray(size: Int): Array<Category?> {
            return arrayOfNulls(size)
        }
    }
}