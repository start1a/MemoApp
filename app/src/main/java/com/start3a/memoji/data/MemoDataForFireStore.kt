package com.start3a.memoji.data

import com.google.firebase.Timestamp
import java.util.*

data class MemoDataForFireStore(
    var id: String? = null,
    var title: String? = null,
    var content: String? = null,
    var summary: String? = null,
    var date: Date? = null,
    var imageFileLinks: ArrayList<ImageFilePathForFireStore>? = null,
    var alarmTimeList: ArrayList<Timestamp>? = null
)