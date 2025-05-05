package com.example.fyp_application

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fyp_application.databinding.ActivityCircleScreenBinding

class CircleScreen : AppCompatActivity() {
    private lateinit var binding: ActivityCircleScreenBinding
    private lateinit var groupAdapter: GroupAdapter
    private var groupNames = mutableListOf(
        "Family Circle", "College Buddies", "Work Team", "Weekend Riders",
        "Gaming Squad", "Book Club", "Gym Group", "School Friends",
        "Cousins", "Neighborhood Watch", "Hiking Team"
    )
    private var filteredGroupNames = groupNames.toMutableList()

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityCircleScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Bottom navigation
        binding.btnHome.setOnClickListener {
            startActivity(Intent(this, HomeScreen::class.java))
        }
        binding.btnIndividual.setOnClickListener {
            startActivity(Intent(this, IndividualScreen::class.java))
        }
        binding.btnPlaces.setOnClickListener {
            startActivity(Intent(this, PlacesScreen::class.java))
        }
        binding.btnAccount.setOnClickListener {
            startActivity(Intent(this, SettingScreen::class.java))
        }

        binding.creategrp.setOnClickListener {
            startActivity(Intent(this, JoinCircleScreen::class.java))
        }
        binding.addgrp.setOnClickListener {
            startActivity(Intent(this, CreateCircleScreen::class.java))
        }
        binding.btnBack.setOnClickListener{
            onBackPressedDispatcher.onBackPressed()
        }

        // RecyclerView setup
        groupAdapter = GroupAdapter(filteredGroupNames)
        binding.groupNamesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.groupNamesRecyclerView.adapter = groupAdapter

        // SearchView setup
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })
    }

    private fun filterList(query: String?) {
        filteredGroupNames.clear()
        if (query.isNullOrEmpty()) {
            filteredGroupNames.addAll(groupNames)
        } else {
            val lowerCaseQuery = query.toLowerCase()
            groupNames.forEach { group ->
                if (group.toLowerCase().contains(lowerCaseQuery)) {
                    filteredGroupNames.add(group)
                }
            }
        }
        groupAdapter.notifyDataSetChanged()
    }

    // Adapter
    inner class GroupAdapter(private val groupList: MutableList<String>) :
        RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

        inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val groupNameText: TextView = itemView.findViewById(R.id.groupNameText)
            val groupIcon: ImageView = itemView.findViewById(R.id.groupIcon)  // Delete icon
            val editIcon: ImageView = itemView.findViewById(R.id.EditIcon)    // Edit icon
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_group, parent, false)
            return GroupViewHolder(view)
        }

        override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
            val groupName = groupList[position]

            holder.groupNameText.text = groupName
            holder.groupIcon.setImageResource(R.drawable.delete)
            holder.editIcon.setImageResource(R.drawable.pencil)

            // Delete button click
            holder.groupIcon.setOnClickListener {
                val dialog = AlertDialog.Builder(holder.itemView.context)
                    .setMessage("Are you sure you want to delete $groupName?")
                    .setPositiveButton("OK") { _, _ ->
                        groupList.removeAt(position)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, groupList.size)
                        Toast.makeText(holder.itemView.context, "$groupName has been deleted", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .create()
                dialog.show()
            }

            // Edit button click
            holder.editIcon.setOnClickListener {
                val context = holder.itemView.context
                val editText = EditText(context)
                editText.setText(groupName)

                val dialog = AlertDialog.Builder(context)
                    .setTitle("Edit Group Name")
                    .setView(editText)
                    .setPositiveButton("Save") { _, _ ->
                        val newName = editText.text.toString().trim()
                        if (newName.isNotEmpty()) {
                            groupList[position] = newName
                            notifyItemChanged(position)
                            Toast.makeText(context, "Group renamed to $newName", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Group name cannot be empty!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .create()
                dialog.show()
            }
        }

        override fun getItemCount(): Int = groupList.size
    }
}
