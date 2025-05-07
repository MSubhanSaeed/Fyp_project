package com.example.fyp_application

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fyp_application.databinding.ActivityGeofenceScreenBinding
import com.example.fyp_application.model.GeoFriend
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException
import java.util.*
import kotlin.concurrent.thread

class GeofenceScreen : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityGeofenceScreenBinding
    private lateinit var mMap: GoogleMap
    private var currentCircle: Circle? = null
    private var currentRadius: Int = 500 // Default radius in meters
    private var currentLocation: LatLng? = null
    private lateinit var geocoder: Geocoder
    private lateinit var suggestionsAdapter: ArrayAdapter<String>
    private var isMapReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGeofenceScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        // Set up delete button
        binding.deletePlaceBtn.setOnClickListener {
            deletePlace()
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
        val defaultLocation = LatLng(33.6844, 73.0479) // Islamabad coordinates
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
            currentCircle = mMap.addCircle(CircleOptions()
                .center(location)
                .radius(currentRadius.toDouble())
                .strokeWidth(5f)
                .strokeColor(0xFFFF0000.toInt())
                .fillColor(0x22FF0000.toInt()))
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
        val geoFriendsList = listOf(
            GeoFriend("Subhan", true, false),
            GeoFriend("Ali Haider", false, false),
            GeoFriend("Kamran", true, true),
            GeoFriend("Aminullah", false, true),
            GeoFriend("Saad Haider", false, false),
            GeoFriend("Arslan", true, false),
            GeoFriend("Hashir", false, true),
            GeoFriend("Hadi", false, false),
        )

        val geoFriendAdapter = Geo_friendAdapter(geoFriendsList)
        binding.GeofriendsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.GeofriendsRecyclerView.adapter = geoFriendAdapter
    }

    private fun savePlace() {
        val placeName = binding.placeName.text.toString()

        if (currentLocation == null) {
            currentLocation = mMap.cameraPosition.target
            updateCircle()
        }

        Toast.makeText(this,
            if (placeName.isNotEmpty()) "Place saved: $placeName"
            else "Unnamed location saved",
            Toast.LENGTH_SHORT).show()
    }

    private fun deletePlace() {
        Toast.makeText(this, "Place deleted", Toast.LENGTH_SHORT).show()
        finish()
    }
}