package com.example.memoappexam.data

import android.graphics.Bitmap

data class MemoData(
    var title: String,
    var text: String,
    var images: MutableList<Bitmap> = mutableListOf()
)