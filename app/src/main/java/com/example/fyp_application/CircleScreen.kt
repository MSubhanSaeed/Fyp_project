package com.example.fyp_application

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fyp_application.databinding.ActivityCircleScreenBinding
import com.example.fyp_application.model.GroupResponse
import com.example.fyp_application.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

        // Navigation buttons
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
            startActivity(Intent(this, AccountScreen::class.java))
        }

        binding.Joincrcle.setOnClickListener {
            startActivity(Intent(this, JoinCircleScreen::class.java))
        }
        binding.Createcrcle.setOnClickListener {
            startActivity(Intent(this, CreateGroupScreen::class.java))
        }

        // RecyclerView setup
        groupAdapter = GroupAdapter(filteredGroups)
        binding.groupNamesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.groupNamesRecyclerView.adapter = groupAdapter

        // Search functionality
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })

        fetchGroupsFromApi()
    }

    private fun fetchGroupsFromApi() {
        val sharPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.getInstance()
                    .getUserApiService()
                    .getGroups(sharPref.getInt("userid", -1))
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
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CircleScreen, "Error: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun filterList(query: String?) {
        filteredGroups.clear()
        if (query.isNullOrEmpty()) {
            filteredGroups.addAll(groups)
        } else {
            val lowerCaseQuery = query.lowercase()
            filteredGroups.addAll(groups.filter { it.name?.lowercase()?.contains(lowerCaseQuery) == true })
        }
        groupAdapter.notifyDataSetChanged()
    }

    inner class GroupAdapter(private val groupList: MutableList<GroupResponse>) :
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
            val group = groupList[position]
            holder.groupNameText.text = group.name ?: "Unnamed Group"
            holder.groupIcon.setImageResource(R.drawable.delete)
            holder.editIcon.setImageResource(R.drawable.pencil)

            // Navigate on group name click
            holder.groupNameText.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, CreateCircleScreen::class.java)
                intent.putExtra("Group_Code", group.code)
                context.startActivity(intent)
            }

            // Delete action
            holder.groupIcon.setOnClickListener {
                val dialog = AlertDialog.Builder(holder.itemView.context)
                    .setMessage("Are you sure you want to delete ${group.name}?")
                    .setPositiveButton("OK") { _, _ ->
                        val indexInGroups = groups.indexOf(group)
                        if (indexInGroups >= 0) groups.removeAt(indexInGroups)
                        groupList.removeAt(position)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, groupList.size)

                        Toast.makeText(
                            holder.itemView.context,
                            "${group.name} has been deleted",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .create()
                dialog.show()
            }

            // Edit action
            holder.editIcon.setOnClickListener {
                val context = holder.itemView.context
                val editText = EditText(context)
                editText.setText(group.name)

                val dialog = AlertDialog.Builder(context)
                    .setTitle("Edit Group Name")
                    .setView(editText)
                    .setPositiveButton("Save") { _, _ ->
                        val newName = editText.text.toString().trim()
                        if (newName.isNotEmpty()) {
                            val indexInGroups = groups.indexOf(group)
                            if (indexInGroups >= 0) {
                                groups[indexInGroups] = group.copy(name = newName)
                            }
                            groupList[position] = group.copy(name = newName)
                            notifyItemChanged(position)

                            Toast.makeText(
                                context,
                                "Group renamed to $newName",
                                Toast.LENGTH_SHORT
                            ).show()
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
