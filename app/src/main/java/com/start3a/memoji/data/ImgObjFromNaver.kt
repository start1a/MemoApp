package com.start3a.memoji.data

class ImgObjFromNaver(
    var lastBuildDate: String = "",
    var total: Int = 0,
    var start: Int = 0,
    var display: Int = 0,
    var items: List<NaverImage>
)

data class NaverImage(
    val title: String,
    val link: String,
    val thumbnail: String,
    val sizeheight: String,
    val sizewidth: String
)