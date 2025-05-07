package com.example.fyp_application

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fyp_application.databinding.ActivityHomeScreenBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior

class HomeScreen : AppCompatActivity(), OnMapReadyCallback {

    private var mGoogleMap: GoogleMap? = null
    private lateinit var binding: ActivityHomeScreenBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        BottomSheetBehavior.from(binding.sheet).apply{
            peekHeight=300
            this.state=BottomSheetBehavior.STATE_COLLAPSED
        }

        // In your HomeScreen's onCreate()
        val friendItems = listOf(
            FriendItem.Friend("Subhan", true),
            FriendItem.Friend("Ali Haider", false),
            FriendItem.Friend("Kamran", true),
            FriendItem.Friend("Aminullah", true),
            FriendItem.Friend("Saad Haider", false),
            FriendItem.Friend("Arslan", false),
            FriendItem.Friend("Hashir", true),
            FriendItem.Friend("Hadi", false),
            FriendItem.AddButton // Add this as the last item
        )

        val adapter = FriendsAdapter(friendItems)
        binding.friendsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.friendsRecyclerView.adapter = adapter


        binding.btnIndividual.setOnClickListener {
            // Create an Intent to start HomeActivity
            val intent = Intent(this, IndividualScreen::class.java)
            startActivity(intent)
        }
        binding.btnPlaces.setOnClickListener {
            // Create an Intent to start HomeActivity
            val intent = Intent(this, PlacesScreen::class.java)
            startActivity(intent)
        }
        binding.btnCircle.setOnClickListener {
            // Create an Intent to start HomeActivity
            val intent = Intent(this, CircleScreen::class.java)
            startActivity(intent)
        }
        binding.btnAccount.setOnClickListener {
            // Create an Intent to start HomeActivity
            val intent = Intent(this, AccountScreen::class.java)
            startActivity(intent)
        }
        binding.btnSettings.setOnClickListener {
            // Create an Intent to start HomeActivity
            val intent = Intent(this, Main_Settings_Screen::class.java)
            startActivity(intent)
        }
        binding.btnNotification.setOnClickListener {
            // Create an Intent to start HomeActivity
            val intent = Intent(this, NotificationScreen::class.java)
            startActivity(intent)
        }
        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Access the map fragment using View Binding
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        if (mapFragment == null) {
            Log.e("HomeScreen", "Map fragment is null")
            // Handle the error (e.g., show a message to the user)
        } else {
            mapFragment.getMapAsync(this)
        }

        // Check and request location permissions
        if (checkLocationPermissions()) {
            startLocationUpdates()
        } else {
            requestLocationPermissions()
        }

        // Initialize and set up the Spinner
        setupSpinner()
    }

    private fun setupSpinner() {
        val spinner: Spinner = binding.dropDownMenu

        val adapter = ArrayAdapter(
            this,
            R.layout.custom_spinner_item,
            GroupRepository.groupNames
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                binding.friendsInCircleTitle.text = "$selectedItem Circle"
                Toast.makeText(this@HomeScreen, "Selected: $selectedItem", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }



    override fun onMapReady(googleMap: GoogleMap) {
        Log.d("HomeScreen", "Map is ready")
        mGoogleMap = googleMap

        // Set the default map type to Normal
        mGoogleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL

        // Enable My Location Button
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mGoogleMap?.isMyLocationEnabled = true
        }

        // Enable Zoom Controls
        mGoogleMap?.uiSettings?.isZoomControlsEnabled = true

        // Enable Compass
        mGoogleMap?.uiSettings?.isCompassEnabled = true
    }

    // Check if location permissions are granted
    private fun checkLocationPermissions(): Boolean {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    // Request location permissions
    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    // Start receiving location updates
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (checkLocationPermissions()) {
            val locationRequest = LocationRequest.create().apply {
                interval = 10000 // Update interval in milliseconds
                fastestInterval = 5000 // Fastest update interval
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let { location ->
                        // Update the map with the user's live location
                        val userLatLng = LatLng(location.latitude, location.longitude)
                        mGoogleMap?.clear() // Clear previous markers
                        val userLocationMarker = mGoogleMap?.addMarker(
                            MarkerOptions().position(userLatLng).title("Me")
                        )
                        userLocationMarker?.showInfoWindow() // Show the title by default
                        mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 16f))
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    // Handle permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                Log.e("HomeScreen", "Location permission denied")
                // Show a rationale or redirect the user to app settings
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop location updates when the activity is destroyed
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}