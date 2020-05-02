package com.start3a.memoji.data

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
    var imageFileLinks: RealmList<MemoImageFilePath> = RealmList()
) : RealmObject()