package com.start3a.memoji.data

data class ImageFilePathForFireStore(
    var uri: String = "",
    var thumbnailPath: String = "",
    var originalPath : String = ""
)