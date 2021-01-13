package com.example.pomocnysasiad.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pomocnysasiad.R
import com.example.pomocnysasiad.model.ChatRequestRecord
import kotlinx.android.synthetic.main.request_chat_row.view.*

class ChatRequestAdapter(
    private val list: List<ChatRequestRecord>,
    private val onChatInteraction: OnChatInteraction,
    private val context: Context
): RecyclerView.Adapter<ChatRequestAdapter.MyViewHolder>() {
    inner class MyViewHolder(view: View): RecyclerView.ViewHolder(view), View.OnClickListener{
        val icon: ImageView = view.findViewById(R.id.requestChatRowCategory)
        val title: TextView = view.findViewById(R.id.requestChatRowTitle)
        val user: TextView = view.findViewById(R.id.requestChatRowUser)
        private val button: ImageButton = view.findViewById(R.id.requestChatRowButton)

        init {
            itemView.setOnClickListener(this)

            button.setOnClickListener {
                if(list[adapterPosition].userName.isNotBlank()){
                    onChatInteraction.onChatClick(list[adapterPosition].id)
                }
            }
        }

        override fun onClick(v: View?) {
                onChatInteraction.onDetailsClick(list[adapterPosition].id)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val row = layoutInflater.inflate(R.layout.request_chat_row, parent, false)
        return MyViewHolder(row)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(list[position].categoryIcon).centerCrop().fitCenter()
            .into(holder.icon)
        holder.title.text = list[position].requestTitle
        holder.user.text = list[position].userName
    }

    override fun getItemCount() = list.size
}