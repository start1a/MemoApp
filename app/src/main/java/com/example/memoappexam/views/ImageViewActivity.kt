package com.example.memoappexam.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.memoappexam.R
import com.github.chrisbanes.photoview.PhotoViewAttacher
import kotlinx.android.synthetic.main.activity_image_view.*

class ImageViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_view)

        Glide.with(this)
            .load(intent.getStringExtra("image"))
            .into(photo_view)
    }
}
