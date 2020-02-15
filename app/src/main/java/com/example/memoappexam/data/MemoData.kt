package com.example.memoappexam.data

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class MemoData(
    @PrimaryKey
    var id: String = UUID.randomUUID().toString(),
    var title: String = "",
    var content: String = "",
    var summary: String = "",
    var date: Date = Date(),
    var images: RealmList<MemoImageData> = RealmList()
) : RealmObject()