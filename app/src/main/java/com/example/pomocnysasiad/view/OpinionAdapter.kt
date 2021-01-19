package com.example.pomocnysasiad.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pomocnysasiad.R
import com.example.pomocnysasiad.model.Opinion

class OpinionAdapter(private val list: List<Opinion>) :
    RecyclerView.Adapter<OpinionAdapter.OpinionViewHolder>() {
    inner class OpinionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val user: TextView = view.findViewById(R.id.opinion_row_user)
        val content: TextView = view.findViewById(R.id.opinion_row_content)
        val rate: RatingBar = view.findViewById(R.id.opinion_row_ratingBar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OpinionViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val row = layoutInflater.inflate(R.layout.opinion_row, parent, false)
        return OpinionViewHolder(row)
    }

    override fun onBindViewHolder(holder: OpinionViewHolder, position: Int) {
        holder.content.text = list[position].content
        holder.user.text = list[position].userName
        holder.rate.rating = list[position].score
    }

    override fun getItemCount() = list.size
}