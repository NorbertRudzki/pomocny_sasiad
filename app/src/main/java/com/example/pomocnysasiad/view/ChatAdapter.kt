package com.example.pomocnysasiad.view

import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pomocnysasiad.R

class ChatAdapter(
        private val list: List<com.example.pomocnysasiad.model.Message>,
        private val myId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class MyMessage(view: View) : RecyclerView.ViewHolder(view) {
        val message: TextView = view.findViewById(R.id.chatMyMessage)
    }

    inner class OthersMessage(view: View) : RecyclerView.ViewHolder(view) {
        val message: TextView = view.findViewById(R.id.chatOthersMessage)
    }

    override fun getItemViewType(position: Int) = if (list[position].userId == myId) 0 else 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return if (viewType == 0) MyMessage(layoutInflater.inflate(R.layout.chat_row_my_message, parent, false))
        else OthersMessage(layoutInflater.inflate(R.layout.chat_row_others_message, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder.itemViewType){
            0 -> {
                holder as MyMessage
                holder.message.text = list[position].content
            }
            1 -> {
                holder as OthersMessage
                holder.message.text = list[position].content
            }
        }
    }

    override fun getItemCount() = list.size
}