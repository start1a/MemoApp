package com.example.memoappexam

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.memoappexam.data.ImageResource
import com.example.memoappexam.data.MemoImageData
import kotlinx.android.synthetic.main.item_image_memo.view.*
import java.net.URL

class ImageListAdapter(private val list : MutableList<MemoImageData>)
    : RecyclerView.Adapter<MemoImageViewHolder>() {

    var editMode: Boolean = false
    var deleteImageList: MutableList<Int> = mutableListOf()
    lateinit var itemClickListener: (image: String) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image_memo, parent, false)
        view.setOnClickListener {
            val imageResource = it.tag as ImageResource
            val index = imageResource.selectedIndex
            if (editMode) {
                if (deleteImageList.contains(index)) deleteImageList.remove(index)
                else deleteImageList.add(index)
                notifyDataSetChanged()
            }
            else itemClickListener(imageResource.url)
        }
        return MemoImageViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    override fun onBindViewHolder(holder: MemoImageViewHolder, position: Int) {
        Glide.with(holder.containerView)
            .load(list[position].image)
            .into(holder.containerView.imageItem)

        val imageResource = ImageResource(list[position].image, position)
        holder.containerView.tag = imageResource

        holder.containerView.deleteCheck.isChecked =
            deleteImageList.contains(imageResource.selectedIndex)
        if (editMode) holder.containerView.deleteCheck.visibility = View.VISIBLE
        else holder.containerView.deleteCheck.visibility = View.GONE
    }
}