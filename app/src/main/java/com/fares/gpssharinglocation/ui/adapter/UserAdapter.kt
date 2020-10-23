package com.fares.gpssharinglocation.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fares.gpssharinglocation.R
import com.fares.gpssharinglocation.model.User
import kotlinx.android.synthetic.main.user_item.view.*

class UserAdapter() : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    private var data: List<User> = ArrayList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun swapData(data: List<User>) {
        this.data = data
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
        holder.itemView.setOnClickListener {
            onItemClickListener?.onItemClick(data[position], position)
        }
        holder.itemView.setOnLongClickListener {
            onItemLongClickListener?.onItemLongClick(data[position], position)
            return@setOnLongClickListener true
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(user: User) = with(itemView) {
            txt_username.text = user.username
            txt_user_phone.text = user.phoneNumber
        }
    }

    private var onItemClickListener: OnItemClickListener? = null
    private var onItemLongClickListener: OnItemLongClickListener? = null


    fun setOnItemClickListener(click: (User, Int) -> Unit): OnItemClickListener {
        onItemClickListener = object : OnItemClickListener {
            override fun onItemClick(user: User, position: Int) {
                click(user, position)
            }
        }
        return onItemClickListener!!
    }

    fun setOnItemLongClickListener(click: (User, Int) -> Unit): OnItemLongClickListener {
        onItemLongClickListener = object : OnItemLongClickListener {
            override fun onItemLongClick(user: User, position: Int) {
                click(user,position)
            }
        }
        return onItemLongClickListener!!
    }

    interface OnItemClickListener {
        fun onItemClick(user: User, position: Int)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(user: User, position: Int)
    }
}