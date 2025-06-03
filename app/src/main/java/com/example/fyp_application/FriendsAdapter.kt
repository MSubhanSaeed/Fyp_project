package com.example.fyp_application

import FriendItem
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.fyp_application.databinding.ItemFriendBinding
import com.google.android.material.switchmaterial.SwitchMaterial
class FriendsAdapter(private val friendList: MutableList<FriendItem.Friend>) :
    RecyclerView.Adapter<FriendsAdapter.FriendViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding = ItemFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friendList[position]
        holder.bind(friend)
    }

    override fun getItemCount(): Int = friendList.size

    fun updateList(newList: MutableList<FriendItem.Friend>) {
        friendList.clear()
        friendList.addAll(newList)
        notifyDataSetChanged()
    }

    inner class FriendViewHolder(private val binding: ItemFriendBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(friend: FriendItem.Friend) {
            binding.friendName.text = friend.name
            binding.trackingSwitch.isChecked = friend.isTrackingAllowed
        }
    }
}
