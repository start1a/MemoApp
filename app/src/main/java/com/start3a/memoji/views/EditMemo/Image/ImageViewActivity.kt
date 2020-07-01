package com.start3a.memoji.views.EditMemo.Image

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.start3a.memoji.R
import com.start3a.memoji.viewmodel.ImageDetailViewModel
import kotlinx.android.synthetic.main.activity_image_view.*

class ImageViewActivity : AppCompatActivity() {

    private var viewModel: ImageDetailViewModel? = null
    private var listAdapter: ImageDetailViewListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_view)
        supportActionBar?.hide()

        viewModel = application!!.let {
            ViewModelProvider(viewModelStore, ViewModelProvider.AndroidViewModelFactory(it))
                .get(ImageDetailViewModel::class.java)
        }
        viewModel!!.let { vm ->
            // intent
            vm.index = intent.getIntExtra("selectedIndex", 0)
            vm.images = intent.getStringArrayListExtra("images") ?: arrayListOf()
            vm.imagesAlternative =
                intent.getStringArrayListExtra("imageAlternative") ?: arrayListOf()

            // 뷰페이저
            listAdapter = ImageDetailViewListAdapter(vm.images, vm.imagesAlternative)
            imageDetailViewPager.run {
                adapter = listAdapter
                currentItem = vm.index
                setPageTransformer { page, position ->
                    vm.index = position.toInt()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 액티비티로 돌아올 때마다 상태 바 가리기
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
    }
}
