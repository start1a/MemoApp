package com.start3a.memoji.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.start3a.memoji.RetrofitService
import com.start3a.memoji.data.NaverImage
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class SearchImageViewModel : ViewModel() {

    var imageList = MutableLiveData<MutableList<NaverImage>>().apply {
        value = mutableListOf()
    }
    var searchText = ""
    private var searchStartLocation = 1
    private val numDisplay = 10

    val selectionList = mutableListOf<NaverImage>()

    fun getImageData(query: String) {
        val list = imageList.value!!
        RetrofitService.getService().requestSearchImage(
            type = "image.json",
            display = numDisplay,
            keyword = query,
            page = searchStartLocation
        ).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { image ->
                list.addAll(image.items)
                updateImageList(list)
                searchStartLocation += numDisplay
            }
    }

    private fun updateImageList(list: MutableList<NaverImage>) {
        imageList.value = list
    }

    fun clearList() {
        searchStartLocation = 1
        imageList.value?.clear()
    }

}
