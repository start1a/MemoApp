package com.example.memoappexam.views

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.memoappexam.R
import com.example.memoappexam.viewmodel.ImageViewModel
import kotlinx.android.synthetic.main.activity_image_view.*

class ImageViewActivity : AppCompatActivity() {

    private var viewModel: ImageViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_view)
        supportActionBar?.hide()

        viewModel = application!!.let {
            ViewModelProvider(viewModelStore, ViewModelProvider.AndroidViewModelFactory(it))
                .get(ImageViewModel::class.java)
        }
        viewModel!!.image = intent.getStringExtra("image")

        Glide.with(this)
            .load(viewModel!!.image)
            .into(photo_view)
    }

    override fun onResume() {
        super.onResume()
        // 액티비티로 돌아올 때마다 상태 바 가리기
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
    }
}
