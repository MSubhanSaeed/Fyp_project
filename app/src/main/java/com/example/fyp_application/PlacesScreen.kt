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
import com.example.fyp_application.databinding.ActivityPlacesScreenBinding
import com.example.fyp_application.dto.IndividualLiveLocationEntity
import com.example.fyp_application.dto.UserGeofence
import com.example.fyp_application.dto.UserLiveLocationEntity
import com.example.fyp_application.network.RetrofitClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.set

class PlacesScreen : AppCompatActivity() {

    private lateinit var binding: ActivityPlacesScreenBinding
    private lateinit var placeAdapter: PlaceAdapter

    companion object {
        /** master list of geofences (id + name) */
        val allGeofences = mutableListOf<UserGeofence>()
    }
    private val filteredGeofences = allGeofences.toMutableList()

    // ─────────────────────────────────────────────────────────────────────────

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityPlacesScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNav()
        setupRecycler()
        setupSearch()
        fetchGeofencesFromApi()

    }

    // ───────────────── Nav / UI helpers ─────────────────────────────────────

    private fun setupNav() {
        binding.bottomNavigation.selectedItemId = R.id.btnPlaces
        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.btnIndividual -> startActivity(Intent(this, IndividualScreen::class.java))
                R.id.btnHome       -> startActivity(Intent(this, HomeScreen::class.java))
                R.id.btnCircle     -> startActivity(Intent(this, CircleScreen::class.java))
                R.id.btnAccount    -> startActivity(Intent(this, AccountScreen::class.java))
            }; true
        }
        binding.btnNotification.setOnClickListener {
            startActivity(Intent(this, NotificationScreen::class.java))
        }
        binding.Addgeofence.setOnClickListener {
            startActivity(Intent(this, GeofenceScreen::class.java))
        }
    }

    private fun setupRecycler() {
        placeAdapter = PlaceAdapter(filteredGeofences)
        binding.placesNamesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@PlacesScreen)
            adapter = placeAdapter
        }
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })
    }


    // ───────────────── Data fetch ───────────────────────────────────────────

    private fun fetchGeofencesFromApi() {
        val userId = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            .getInt("userid", -1)

        RetrofitClient.getInstance().getUserApiService()
            .getGeofencesByUser(userId)
            .enqueue(object : Callback<List<UserGeofence>> {
                override fun onResponse(
                    call: Call<List<UserGeofence>>,
                    resp: Response<List<UserGeofence>>
                ) {
                    if (resp.isSuccessful) {
                        allGeofences.apply {
                            clear()
                            addAll(resp.body().orEmpty())
                        }
                        filterList(binding.searchView.query?.toString())
                    } else
                        Toast.makeText(this@PlacesScreen, "Failed to fetch places", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(call: Call<List<UserGeofence>>, t: Throwable) {
                    Toast.makeText(this@PlacesScreen, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

 // ───────────────── Filter helper ────────────────────────────────────────

    private fun filterList(query: String?) {
        filteredGeofences.run {
            clear()
            if (query.isNullOrBlank()) addAll(allGeofences)
            else {
                val q = query.lowercase()
                addAll(allGeofences.filter { it.Name.lowercase().contains(q) })
            }
        }
        placeAdapter.notifyDataSetChanged()
    }

    // ───────────────── Adapter ──────────────────────────────────────────────

    inner class PlaceAdapter(private val items: MutableList<UserGeofence>) :
        RecyclerView.Adapter<PlaceAdapter.VH>() {

        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val name: TextView  = v.findViewById(R.id.placeNameText)
            val edit: ImageView = v.findViewById(R.id.editIcon)
            val del : ImageView = v.findViewById(R.id.deleteIcon)
        }

        override fun onCreateViewHolder(p: ViewGroup, vt: Int) =
            VH(LayoutInflater.from(p.context).inflate(R.layout.item_places, p, false))

        override fun getItemCount() = items.size

        override fun onBindViewHolder(h: VH, pos: Int) {
            val geo = items[pos]
            h.name.text = geo.Name
            h.edit.setOnClickListener { showEditDialog(pos) }
            h.del .setOnClickListener { showDeleteDialog(pos) }
        }

        // ───── Edit ─────
        private fun showEditDialog(index: Int) {
            val ctx = this@PlacesScreen
            val edit = EditText(ctx).apply { setText(items[index].Name) }

            AlertDialog.Builder(ctx)
                .setTitle("Edit Place Name")
                .setView(edit)
                .setPositiveButton("Save") { _, _ ->
                    val newName = edit.text.toString().trim()
                    if (newName.isNotEmpty()) {
                        // update both filtered + master list
                        items[index] = items[index].copy(Name = newName)
                        val masterPos = allGeofences.indexOfFirst { it.GroupId == items[index].GroupId }
                        if (masterPos >= 0) allGeofences[masterPos] = items[index]
                        notifyItemChanged(index)
                        Toast.makeText(ctx, "Renamed to \"$newName\"", Toast.LENGTH_SHORT).show()
                    } else Toast.makeText(ctx, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // ───── Delete ─────
        private fun showDeleteDialog(index: Int) {
            val ctx   = this@PlacesScreen
            val geo   = items[index]
            val api   = RetrofitClient.getInstance().getUserApiService()

            AlertDialog.Builder(ctx)
                .setMessage("Delete \"${geo.Name}\"?")
                .setPositiveButton("Delete") { _, _ ->
                    api.DeleteGeofence(geo.Id).enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, resp: Response<Void>) {
                            if (resp.isSuccessful) {
                                // remove from both lists
                                val masterPos = allGeofences.indexOfFirst { it.Id == geo.Id }
                                if (masterPos >= 0) allGeofences.removeAt(masterPos)
                                items.removeAt(index)
                                notifyItemRemoved(index)
                                Toast.makeText(ctx, "\"${geo.Name}\" deleted", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(ctx, "Failed to delete (HTTP ${resp.code()})", Toast.LENGTH_LONG).show()
                            }
                        }
                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Toast.makeText(ctx, "Error: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    })
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}
