package com.example.fyp_application

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.fyp_application.model.Friend

class FriendsAdapter(
    private val friends: List<Friend>
) : RecyclerView.Adapter<FriendsAdapter.FriendViewHolder>() {

    inner class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.friendName)
        val trackingSwitch: Switch = itemView.findViewById(R.id.trackingSwitch)
        val switchLabel: TextView = itemView.findViewById(R.id.switchLabel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friends[position]

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

    override fun getItemCount(): Int = friends.size
}
