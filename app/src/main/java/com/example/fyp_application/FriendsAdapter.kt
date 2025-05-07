package com.example.fyp_application

import FriendItem
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class FriendsAdapter(
    private val items: List<FriendItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_FRIEND = 0
        private const val TYPE_ADD_BUTTON = 1
    }

    inner class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.friendName)
        val trackingSwitch: Switch = itemView.findViewById(R.id.trackingSwitch)
        val switchLabel: TextView = itemView.findViewById(R.id.switchLabel)
    }

    inner class AddButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val addButton: Button = itemView.findViewById(R.id.addFriendButton)
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is FriendItem.Friend -> TYPE_FRIEND
            FriendItem.AddButton -> TYPE_ADD_BUTTON
            else -> throw IllegalStateException("Unknown view type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_FRIEND -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_friend, parent, false)
                FriendViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_add_friend_button, parent, false)
                AddButtonViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FriendViewHolder -> {
                val friend = items[position] as FriendItem.Friend
                holder.nameText.text = friend.name
                holder.trackingSwitch.isChecked = friend.isTrackingAllowed
                holder.switchLabel.text = if (friend.isTrackingAllowed) "Tracking Allowed" else "Request Tracking"

                holder.trackingSwitch.setOnCheckedChangeListener { _, isChecked ->
                    friend.isTrackingAllowed = isChecked
                    holder.switchLabel.text = if (isChecked) "Tracking Allowed" else "Request Tracking"
                    Toast.makeText(
                        holder.itemView.context,
                        if (isChecked) "${friend.name} tracking allowed" else "Requested tracking for ${friend.name}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            is AddButtonViewHolder -> {
                holder.addButton.setOnClickListener {
                    Toast.makeText(holder.itemView.context, "Add Friend clicked", Toast.LENGTH_SHORT).show()
                    // Handle add friend logic here
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size
}