package com.example.fyp_application

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fyp_application.databinding.ActivityCircleScreenBinding
import com.example.fyp_application.databinding.ActivityPlacesScreenBinding
import com.google.android.gms.location.Geofence


class PlacesScreen : AppCompatActivity() {
    private lateinit var binding: ActivityPlacesScreenBinding
    private lateinit var groupAdapter: GroupAdapter
    private val placesNames = listOf(
        "Karachi", "Lahore", "Islamabad", "Rawalpindi", "Peshawar",
        "Quetta", "Faisalabad", "Multan", "Sialkot", "Gujranwala",
        "Bahawalpur", "Sargodha", "Murree", "Hunza", "Skardu"
    )

    private var filteredGroupNames = placesNames.toMutableList() // List for filtered data

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize ViewBinding
        binding = ActivityPlacesScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up bottom navigation button listeners
        binding.btnHome.setOnClickListener {
            startActivity(Intent(this, HomeScreen::class.java))
        }

        binding.btnIndividual.setOnClickListener {
            startActivity(Intent(this, IndividualScreen::class.java))
        }

        binding.btnCircle.setOnClickListener {
            startActivity(Intent(this, CircleScreen::class.java))
        }

        binding.btnAccount.setOnClickListener {
            startActivity(Intent(this, SettingScreen::class.java))
        }

        binding.Addgeofence.setOnClickListener {
            startActivity(Intent(this, GeofenceScreen::class.java))
        }

        // Set up RecyclerView
        groupAdapter = GroupAdapter(filteredGroupNames)
        binding.groupNamesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.groupNamesRecyclerView.adapter = groupAdapter

        // Set up SearchView and handle query text changes
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText) // Call filter function when text changes
                return true
            }
        })
    }

    // Function to filter the list based on search text
    private fun filterList(query: String?) {
        filteredGroupNames.clear()
        if (query.isNullOrEmpty()) {
            filteredGroupNames.addAll(placesNames) // Show all items if search is empty
        } else {
            val lowerCaseQuery = query.toLowerCase()
            placesNames.forEach { group ->
                if (group.toLowerCase().contains(lowerCaseQuery)) {
                    filteredGroupNames.add(group) // Add item if it matches the query
                }
            }
        }
        groupAdapter.notifyDataSetChanged() // Notify adapter to update RecyclerView
    }

    // Adapter class for RecyclerView
    class GroupAdapter(private val groupList: List<String>) :
        RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

        inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val groupNameText: TextView = itemView.findViewById(R.id.groupNameText)
            val groupIcon: ImageView = itemView.findViewById(R.id.groupIcon)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_group, parent, false)
            return GroupViewHolder(view)
        }

        override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
            val groupName = groupList[position]

            holder.groupNameText.text = groupName
            holder.groupIcon.setImageResource(R.drawable.delete) // Replace with your actual image

            // Image click logic
            holder.groupIcon.setOnClickListener {
                // Show confirmation dialog
                val dialog = AlertDialog.Builder(holder.itemView.context)
                    .setMessage("Are you sure you want to delete $groupName?")
                    .setPositiveButton("OK") { _, _ ->
                        // Delete the group name from the list
                        (groupList as MutableList).removeAt(position)  // Remove item at that position
                        notifyItemRemoved(position)  // Notify the adapter that item has been removed
                        notifyItemRangeChanged(position, groupList.size) // Update the rest of the list
                        Toast.makeText(
                            holder.itemView.context,
                            "$groupName has been deleted",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .create()

                dialog.show()  // Display the dialog
            }
        }

        override fun getItemCount(): Int = groupList.size
    }
}
