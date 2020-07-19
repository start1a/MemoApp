package com.start3a.memoji.data

import io.realm.RealmObject

// 이미지 저장 경로 데이터
open class MemoImageFilePath(
    var uri: String = "",
    var thumbnailPath: String = "",
    var originalPath : String = ""
): RealmObject() {
    override fun equals(other: Any?): Boolean {
        val o = other as MemoImageFilePath
        return uri == o.uri && thumbnailPath == o.thumbnailPath && originalPath == o.originalPath
    }
}