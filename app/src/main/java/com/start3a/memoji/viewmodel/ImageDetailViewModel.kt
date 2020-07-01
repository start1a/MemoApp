package com.start3a.memoji.viewmodel

import androidx.lifecycle.ViewModel

class ImageDetailViewModel : ViewModel() {

    var index = 0
    lateinit var images: ArrayList<String>
    lateinit var imagesAlternative: ArrayList<String>

}