package com.start3a.memoji.data

import com.google.firebase.Timestamp
import java.util.*

data class MemoDataFS(
    var id: String = UUID.randomUUID().toString(),
    var title: String = "",
    var content: String = "",
    var summary: String = "",
    var date: Date = Date(),
    var imageFileLinks: ArrayList<ImgFilePathForFireStore> = arrayListOf(),
    var alarmTimeList: ArrayList<Timestamp> = arrayListOf(),
    var category: String = ""
)

data class CategoryFS(
    var id: String = UUID.randomUUID().toString(),
    var nameCat: String = ""
)