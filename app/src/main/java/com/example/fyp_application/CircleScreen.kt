package com.example.fyp_application

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
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
import com.example.fyp_application.network.RetrofitClient
import com.example.fyp_application.response.GroupResponse
import kotlinx.coroutines.*

class CircleScreen : AppCompatActivity() {
    private lateinit var binding: ActivityCircleScreenBinding
    private lateinit var groupAdapter: GroupAdapter

    private val groups = mutableListOf<GroupResponse>()
    private val filteredGroups = mutableListOf<GroupResponse>()

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityCircleScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Bottom navigation
        binding.bottomNavigation.selectedItemId = R.id.btnCircle
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.btnIndividual -> startActivity(Intent(this, IndividualScreen::class.java))
                R.id.btnPlaces -> startActivity(Intent(this, PlacesScreen::class.java))
                R.id.btnHome -> startActivity(Intent(this, HomeScreen::class.java))
                R.id.btnAccount -> startActivity(Intent(this, AccountScreen::class.java))
            }
            true
        }

        // Create/join circle buttons
        binding.Joincrcle.setOnClickListener {
            startActivity(Intent(this, JoinCircleScreen::class.java))
        }
        binding.Createcrcle.setOnClickListener {
            startActivity(Intent(this, CreateGroupScreen::class.java))
        }
        binding.btnNotification.setOnClickListener {
            startActivity(Intent(this, NotificationScreen::class.java))
        }

        // RecyclerView setup
        groupAdapter = GroupAdapter(filteredGroups)
        binding.groupNamesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.groupNamesRecyclerView.adapter = groupAdapter

        // Search functionality
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })

        fetchGroupsFromApi()
    }

    private fun fetchGroupsFromApi() {
        val sharPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userId = sharPref.getInt("userid", -1)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.getInstance()
                    .getUserApiService()
                    .getGroups(userId)
                    .execute()

                if (response.isSuccessful && response.body() != null) {
                    val apiGroups = response.body()!!

                    withContext(Dispatchers.Main) {
                        groups.clear()
                        groups.addAll(apiGroups)
                        filteredGroups.clear()
                        filteredGroups.addAll(groups)
                        groupAdapter.notifyDataSetChanged()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@CircleScreen,
                            "Failed to load groups: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CircleScreen, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun filterList(query: String?) {
        filteredGroups.clear()
        if (query.isNullOrEmpty()) {
            filteredGroups.addAll(groups)
        } else {
            val lower = query.lowercase()
            filteredGroups.addAll(groups.filter {
                it.name?.lowercase()?.contains(lower) == true
            })
        }
        groupAdapter.notifyDataSetChanged()
    }

    private fun showDeleteDialog(group: GroupResponse, position: Int) {
        AlertDialog.Builder(this)
            .setMessage("Delete \"${group.name}\"?")
            .setPositiveButton("Delete") { _, _ ->
                val indexInGroups = groups.indexOf(group)
                if (indexInGroups >= 0) groups.removeAt(indexInGroups)
                filteredGroups.removeAt(position)
                groupAdapter.notifyItemRemoved(position)
                Toast.makeText(this, "${group.name} deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel") { _, _ ->
                groupAdapter.notifyItemChanged(position)
            }
            .show()
    }

    private fun showUpdateDialog(group: GroupResponse, position: Int) {
        val editText = EditText(this).apply {
            setText(group.name)
        }

        AlertDialog.Builder(this)
            .setTitle("Rename Group")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty()) {
                    val updatedGroup = group.copy(name = newName)
                    val indexInGroups = groups.indexOf(group)
                    if (indexInGroups >= 0) groups[indexInGroups] = updatedGroup
                    filteredGroups[position] = updatedGroup
                    groupAdapter.notifyItemChanged(position)
                    Toast.makeText(this, "Renamed to $newName", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Group name cannot be empty", Toast.LENGTH_SHORT).show()
                    groupAdapter.notifyItemChanged(position)
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                groupAdapter.notifyItemChanged(position)
            }
            .show()
    }

    inner class GroupAdapter(private val groupList: MutableList<GroupResponse>) :
        RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

        inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val groupNameText: TextView = itemView.findViewById(R.id.groupNameText)
            val editIcon: ImageView = itemView.findViewById(R.id.editIcon)
            val deleteIcon: ImageView = itemView.findViewById(R.id.deleteIcon)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group, parent, false)
            return GroupViewHolder(view)
        }

        override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
            val group = groupList[position]
            holder.groupNameText.text = group.name

            holder.groupNameText.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, InvitecodeScreen::class.java)
                intent.putExtra("Group_Code", group.code)
                context.startActivity(intent)
            }

            // Handle edit click
            holder.editIcon.setOnClickListener {
                showUpdateDialog(group, position)
            }

            // Handle delete click
            holder.deleteIcon.setOnClickListener {
                showDeleteDialog(group, position)
            }
        }

        override fun getItemCount(): Int = groupList.size
    }
}
