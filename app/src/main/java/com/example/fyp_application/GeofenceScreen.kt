package com.example.fyp_application

import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fyp_application.Adapters.Geo_friendAdapter
import com.example.fyp_application.databinding.ActivityGeofenceScreenBinding
import com.example.fyp_application.dto.GeofenceFriend
import com.example.fyp_application.dto.UserEntity
import com.example.fyp_application.dto.UserGeofence
import com.example.fyp_application.model.GeoFriend
import com.example.fyp_application.network.RetrofitClient
import com.example.fyp_application.response.GroupResponse
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class GeofenceScreen : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityGeofenceScreenBinding
    private lateinit var mMap: GoogleMap
    private var currentCircle: Circle? = null
    private var currentRadius: Int = 5
    private var currentLocation: LatLng? = null
    private lateinit var geocoder: Geocoder
    private lateinit var suggestionsAdapter: ArrayAdapter<String>
    private var isMapReady = false
    private lateinit var geoFriendAdapter: Geo_friendAdapter
    private var currentGroupId: Int = -1
    private val groups = mutableListOf<GroupResponse>()
    private lateinit var spinnerAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGeofenceScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize UI components
        setupUI()

        // Get group ID from intent if available
        currentGroupId = intent.getIntExtra("GROUP_ID", -1)

        geocoder = Geocoder(this, Locale.getDefault())

        // Initialize autocomplete suggestions
        setupLocationAutocomplete()

        // Set up back button
        binding.backArrow.setOnClickListener {
            finish()
        }

        // Set up save button
        binding.saveButton.setOnClickListener {
            savePlace()
        }

        // Initialize map fragment
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Set up seekbar for radius adjustment
        binding.radiusSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                currentRadius = progress
                updateRadiusLabel()
                updateCircle()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Initialize radius label
        updateRadiusLabel()

        // Set up the "Get notified when..." recycler view
        setupRecyclerView()

        // Fetch groups for the spinner
        fetchGroupsFromApi()
    }

    private fun setupUI() {
        // Setup spinner adapter
        spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            mutableListOf()
        )
        binding.dropDownMenu.adapter = spinnerAdapter

        binding.dropDownMenu.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedGroup = groups[position]
                currentGroupId = selectedGroup.groupId
                fetchGroupMembers(selectedGroup.groupId)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun fetchGroupsFromApi() {
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userId = sharedPref.getInt("userid", -1)

        RetrofitClient.getInstance().getUserApiService()
            .getGroups(userId)
            .enqueue(object : Callback<List<GroupResponse>> {
                override fun onResponse(
                    call: Call<List<GroupResponse>>,
                    response: Response<List<GroupResponse>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        groups.clear()
                        groups.addAll(response.body()!!)

                        spinnerAdapter.clear()
                        spinnerAdapter.addAll(groups.map { it.name ?: "Unnamed Group" })
                        spinnerAdapter.notifyDataSetChanged()

                        // If we have a group ID from intent, select it in the spinner
                        if (currentGroupId != -1) {
                            val index = groups.indexOfFirst { it.groupId == currentGroupId }
                            if (index != -1) {
                                binding.dropDownMenu.setSelection(index)
                            }
                        }
                    } else {
                        Toast.makeText(
                            this@GeofenceScreen,
                            "Failed to load groups",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<GroupResponse>>, t: Throwable) {
                    Toast.makeText(
                        this@GeofenceScreen,
                        "Error: ${t.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun fetchGroupMembers(groupId: Int) {
        RetrofitClient.getInstance().getUserApiService()
            .getUsersInGroup(groupId)
            .enqueue(object : Callback<List<UserEntity>> {
                override fun onResponse(call: Call<List<UserEntity>>, response: Response<List<UserEntity>>) {
                    if (response.isSuccessful && response.body() != null) {
                        val users = response.body()!!
                        val geoFriends = users.map { user ->
                            GeoFriend(
                                userId = user.userId,
                                name = user.name ?: "Unknown"
                            )
                        }
                        geoFriendAdapter.updateList(geoFriends)
                    } else {
                        Toast.makeText(
                            this@GeofenceScreen,
                            "Failed to load group members",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<UserEntity>>, t: Throwable) {
                    Toast.makeText(
                        this@GeofenceScreen,
                        "Error: ${t.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun setupLocationAutocomplete() {
        suggestionsAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            mutableListOf() // Start with empty list
        )

        binding.placeName.apply {
            threshold = 3 // Minimum characters to trigger suggestions
            setAdapter(suggestionsAdapter)

            setOnItemClickListener { _, _, position, _ ->
                val selectedLocation = suggestionsAdapter.getItem(position)
                selectedLocation?.let {
                    setText(it)
                    searchLocation(it)
                }
            }

            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    s?.let {
                        if (it.length >= 3) {
                            fetchLocationSuggestions(it.toString())
                        } else {
                            suggestionsAdapter.clear()
                        }
                    }
                }
            })
        }
    }

    private fun fetchLocationSuggestions(query: String) {
        thread {
            try {
                val addresses = geocoder.getFromLocationName(query, 5)
                val newSuggestions = addresses?.mapNotNull { address ->
                    address.getAddressLine(0)
                } ?: emptyList()

                runOnUiThread {
                    suggestionsAdapter.clear()
                    suggestionsAdapter.addAll(newSuggestions)
                    suggestionsAdapter.notifyDataSetChanged()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Error fetching suggestions", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        isMapReady = true

        // Enable zoom controls
        mMap.uiSettings.isZoomControlsEnabled = true

        // Set default location
        val defaultLocation = LatLng(33.631643627, 73.0693732) //Home Coordinates
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))

        // Add initial circle
        currentLocation = defaultLocation
        updateCircle()

        // Click listener to move the circle
        mMap.setOnMapClickListener { latLng ->
            currentLocation = latLng
            updateCircle()
            addMarkerAtLocation(latLng)
        }

        // Long click alternative
        mMap.setOnMapLongClickListener { latLng ->
            currentLocation = latLng
            updateCircle()
            addMarkerAtLocation(latLng)
        }
    }

    private fun updateRadiusLabel() {
        binding.radiusLabel.text = "$currentRadius meter\nRadius"
    }

    private fun updateCircle() {
        if (!isMapReady) return

        currentLocation?.let { location ->
            currentCircle?.remove()
            currentCircle = mMap.addCircle(
                CircleOptions()
                    .center(location)
                    .radius(currentRadius.toDouble())
                    .strokeWidth(5f)
                    .strokeColor(0xFFFF0000.toInt())
                    .fillColor(0x22FF0000.toInt())
            )
        }
    }

    private fun addMarkerAtLocation(latLng: LatLng) {
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(latLng))
    }

    private fun searchLocation(locationName: String) {
        if (locationName.isEmpty()) return

        thread {
            try {
                val addresses = geocoder.getFromLocationName(locationName, 1)
                runOnUiThread {
                    if (addresses != null && addresses.isNotEmpty()) {
                        val address = addresses[0]
                        val latLng = LatLng(address.latitude, address.longitude)
                        currentLocation = latLng
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                        addMarkerAtLocation(latLng)
                        updateCircle()
                    } else {
                        Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Error searching location", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupRecyclerView() {
        geoFriendAdapter = Geo_friendAdapter(mutableListOf())
        binding.GeofriendsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.GeofriendsRecyclerView.adapter = geoFriendAdapter
    }

    private fun savePlace() {
        val placeName = binding.placeName.text.toString().trim()
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userId = sharedPref.getInt("userid", -1)

        // Validation checks
        if (placeName.isEmpty()) {
            Toast.makeText(this, "Please enter a name for the place", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentLocation == null) {
            Toast.makeText(this, "Please select a location on the map", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentGroupId == -1) {
            Toast.makeText(this, "Please select a group", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading state
        binding.saveButton.isEnabled = false

        // Prepare friends list
        val friendsList = geoFriendAdapter.getCurrentList().map { friend ->
            GeofenceFriend(
                FriendId = friend.userId,
                Arrive = friend.isArrived,
                Leave = friend.isLeaved
            )
        }

        // Create request object
        val geofenceRequest = UserGeofence(
            Id = 0,
            UserId = userId,
            GroupId = currentGroupId,
            Latitude = currentLocation!!.latitude,
            Longitude = currentLocation!!.longitude,
            Radius = currentRadius.toDouble(),
            Name = placeName,
            Friends = friendsList,

        )

        // Make API call
        RetrofitClient.getInstance().getUserApiService()
            .addGeofence(geofenceRequest)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    binding.saveButton.isEnabled = true
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@GeofenceScreen,
                            "Geofence created successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        setResult(Activity.RESULT_OK)
                        finish()
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("API_ERROR", "Failed to create geofence: $errorBody")
                        Toast.makeText(
                            this@GeofenceScreen,
                            "Geofence created but server reported an issue",
                            Toast.LENGTH_SHORT
                        ).show()
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    binding.saveButton.isEnabled = true
                    Log.e("API_FAILURE", "Network error", t)
                    Toast.makeText(
                        this@GeofenceScreen,
                        "Network error: ${t.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}