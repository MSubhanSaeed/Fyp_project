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
import com.example.fyp_application.databinding.ActivityPlacesScreenBinding

class PlacesScreen : AppCompatActivity() {
    private lateinit var binding: ActivityPlacesScreenBinding
    private lateinit var groupAdapter: GroupAdapter

    private val placesNames = mutableListOf(
        "Karachi", "Lahore", "Islamabad", "Rawalpindi", "Peshawar",
        "Quetta", "Faisalabad", "Multan", "Sialkot", "Gujranwala",
        "Bahawalpur", "Sargodha", "Murree", "Hunza", "Skardu"
    )

    private var filteredGroupNames = placesNames.toMutableList()

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityPlacesScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        binding.btnBack.setOnClickListener{
            onBackPressedDispatcher.onBackPressed()
        }

        groupAdapter = GroupAdapter(filteredGroupNames)
        binding.groupNamesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.groupNamesRecyclerView.adapter = groupAdapter

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })
    }

    private fun filterList(query: String?) {
        filteredGroupNames.clear()
        if (query.isNullOrEmpty()) {
            filteredGroupNames.addAll(placesNames)
        } else {
            val lowerCaseQuery = query.lowercase()
            placesNames.forEach { group ->
                if (group.lowercase().contains(lowerCaseQuery)) {
                    filteredGroupNames.add(group)
                }
            }
        }
        groupAdapter.notifyDataSetChanged()
    }

    class GroupAdapter(private val groupList: MutableList<String>) :
        RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

        inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val groupNameText: TextView = itemView.findViewById(R.id.groupNameText)
            val groupIcon: ImageView = itemView.findViewById(R.id.groupIcon)
            val editIcon: ImageView = itemView.findViewById(R.id.EditIcon)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_group, parent, false)
            return GroupViewHolder(view)
        }

        override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
            val groupName = groupList[position]

            holder.groupNameText.text = groupName

            // Delete functionality
            holder.groupIcon.setOnClickListener {
                val dialog = AlertDialog.Builder(holder.itemView.context)
                    .setMessage("Are you sure you want to delete $groupName?")
                    .setPositiveButton("OK") { _, _ ->
                        groupList.removeAt(position)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, groupList.size)
                        Toast.makeText(
                            holder.itemView.context,
                            "$groupName has been deleted",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .create()

                dialog.show()
            }

            // Edit functionality
            holder.editIcon.setOnClickListener {
                val context = holder.itemView.context

                val editText = EditText(context)
                editText.setText(groupName)

                val dialog = AlertDialog.Builder(context)
                    .setTitle("Edit Group Name")
                    .setView(editText)
                    .setPositiveButton("Save") { _, _ ->
                        val newGroupName = editText.text.toString()
                        if (newGroupName.isNotEmpty()) {
                            groupList[position] = newGroupName
                            notifyItemChanged(position)
                            Toast.makeText(
                                context,
                                "Group name updated to $newGroupName",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                "Group name cannot be empty",
                                Toast.LENGTH_SHORT
                            ).show()
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
