package com.start3a.memoji.views.Category

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.start3a.memoji.ListViewHolder
import com.start3a.memoji.R
import com.start3a.memoji.data.Category
import kotlinx.android.synthetic.main.item_category.view.*

class CategoryAdapter(private val list: MutableList<Category>) :
    RecyclerView.Adapter<ListViewHolder>() {

    lateinit var itemClickListener: (selectedCat: Category) -> Unit
    lateinit var itemDeleteListener: (selectedCat: Category) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)

        view.setOnClickListener {
            val cat = it.tag as Category
            itemClickListener(cat)
        }
        return ListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.containerView.run {
            imageDelete.setOnClickListener {
                itemDeleteListener(list[position])
            }
            textCatName.text = list[position].nameCat
            tag = list[position]
        }
    }
}