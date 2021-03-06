package com.example.pomocnysasiad.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pomocnysasiad.R


class CategoryAdapter(
    private val context: Context,
    private val list: List<HashMap<String, Any>>,
    private val onCategorySelected: OnCategorySelected,
    private val selectedOne: Int?
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
        val image: ImageView = v.findViewById(R.id.categoryListRowImage)
        val name: TextView = v.findViewById(R.id.categoryRowName)
        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            onCategorySelected.onCategorySelected(adapterPosition)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.category_item, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.image.setImageResource(list[position]["image"] as Int)
        holder.image.setColorFilter(ContextCompat.getColor(context, R.color.pDark))
        holder.name.text = list[position]["name"] as CharSequence
//        Glide.with(context).load(list[position]["image"]).centerCrop().fitCenter()
//            .into(holder.image)
        selectedOne?.let {
            if(position==selectedOne) {
//                holder.layout.setBackgroundColor(ContextCompat.getColor(context, R.color.primary))
                holder.image.setColorFilter(ContextCompat.getColor(context,R.color.secondary))
            }
        }
    }

    override fun getItemCount() = list.size
}