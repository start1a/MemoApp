package com.example.memoappexam.data

import io.realm.RealmObject

open class MemoImageFilePath(
    var uri: String = "",
    var thumbnailPath: String = "",
    var originalPath : String = ""
): RealmObject()