package com.example.fyp_application

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.fyp_application.model.GeoFriend

class  Geo_friendAdapter(
    private val geofriends: List<GeoFriend>
) : RecyclerView.Adapter<Geo_friendAdapter.GeoFriendViewHolder>() {

    inner class GeoFriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.friendName)
        val arriveSwitch: Switch = itemView.findViewById(R.id.arrive)
        val leaveSwitch: Switch = itemView.findViewById(R.id.leave)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GeoFriendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.geofence_friends, parent, false)
        return GeoFriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: GeoFriendViewHolder, position: Int) {
        val friend = geofriends[position]

        holder.nameText.text = friend.name
        holder.arriveSwitch.isChecked = friend.isArrived
        holder.leaveSwitch.isChecked = friend.isLeaved

        holder.arriveSwitch.setOnCheckedChangeListener { _, isChecked ->
            friend.isArrived = isChecked
            Toast.makeText(
                holder.itemView.context,
                if (isChecked) "Arrival notification enabled for ${friend.name}"
                else "Arrival notification disabled for ${friend.name}",
                Toast.LENGTH_SHORT
            ).show()
        }

        holder.leaveSwitch.setOnCheckedChangeListener { _, isChecked ->
            friend.isLeaved = isChecked
            Toast.makeText(
                holder.itemView.context,
                if (isChecked) "Leave notification enabled for ${friend.name}"
                else "Leave notification disabled for ${friend.name}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun getItemCount(): Int = geofriends.size
}
