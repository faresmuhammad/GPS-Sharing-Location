package com.fares.gpssharinglocation.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fares.gpssharinglocation.BR
import com.fares.gpssharinglocation.databinding.ProfileItemBinding
import com.fares.gpssharinglocation.model.Profile

class ProfileAdapter() : RecyclerView.Adapter<ProfileAdapter.ViewHolder>() {

    private var data: List<Profile> = ArrayList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemBinding = ProfileItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(itemBinding)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun swapData(data: List<Profile>) {
        this.data = data
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
        holder.itemView.setOnClickListener {
            onItemClickListener?.onItemClick(data[position], position)
        }
    }

    class ViewHolder(private val binding: ProfileItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(profile: Profile) {
            binding.apply {
                setVariable(BR.profile, profile)
                executePendingBindings()
            }
            binding.txtProfileName.text = profile.name

        }
    }

    private var onItemClickListener: OnItemClickListener? = null


    fun setOnItemClickListener(click: (Profile, Int) -> Unit): OnItemClickListener {
        onItemClickListener = object : OnItemClickListener {
            override fun onItemClick(profile: Profile, position: Int) {
                click(profile, position)
            }
        }
        return onItemClickListener!!
    }

    interface OnItemClickListener {
        fun onItemClick(profile: Profile, position: Int)
    }
}