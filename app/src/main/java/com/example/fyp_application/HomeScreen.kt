package com.example.fyp_application

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fyp_application.databinding.ActivityHomeScreenBinding
import com.example.fyp_application.model.GroupResponse
import com.example.fyp_application.network.RetrofitClient
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeScreen : AppCompatActivity(), OnMapReadyCallback {

    private var mGoogleMap: GoogleMap? = null
    private lateinit var binding: ActivityHomeScreenBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private val groups = mutableListOf<GroupResponse>()
    private lateinit var spinnerAdapter: ArrayAdapter<String>
    private lateinit var friendsAdapter: FriendsAdapter  // Adapter reused for updates

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        BottomSheetBehavior.from(binding.sheet).apply {
            peekHeight = 500
            state = BottomSheetBehavior.STATE_COLLAPSED
        }

        binding.friendsRecyclerView.layoutManager = LinearLayoutManager(this)
        friendsAdapter = FriendsAdapter(mutableListOf())
        binding.friendsRecyclerView.adapter = friendsAdapter

        // Navigation buttons
        binding.btnIndividual.setOnClickListener { startActivity(Intent(this, IndividualScreen::class.java)) }
        binding.btnPlaces.setOnClickListener { startActivity(Intent(this, PlacesScreen::class.java)) }
        binding.btnCircle.setOnClickListener { startActivity(Intent(this, CircleScreen::class.java)) }
        binding.btnAccount.setOnClickListener { startActivity(Intent(this, AccountScreen::class.java)) }
        binding.btnSettings.setOnClickListener { startActivity(Intent(this, Main_Settings_Screen::class.java)) }
        binding.btnNotification.setOnClickListener { startActivity(Intent(this, NotificationScreen::class.java)) }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        setupSpinner()
        fetchGroupsFromApi()

        if (checkLocationPermissions()) {
            if (isLocationEnabled()) {
                startLocationUpdates()
            } else {
                promptEnableLocation()
            }
        } else {
            requestLocationPermissions()
        }
    }

    private fun setupSpinner() {
        spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            mutableListOf()
        )
        binding.dropDownMenu.adapter = spinnerAdapter

        binding.dropDownMenu.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedGroup = groups[position]
                binding.friendsInCircleTitle.text = "${selectedGroup.name} Circle"
                fetchUsersInGroup(selectedGroup.groupId)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
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
                        spinnerAdapter.clear()
                        spinnerAdapter.addAll(groups.map { it.name ?: "Unnamed Group" })
                        spinnerAdapter.notifyDataSetChanged()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@HomeScreen, "Failed to load groups", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HomeScreen, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fetchUsersInGroup(groupId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.getInstance()
                    .getUserApiService()
                    .getUserInGroup(groupId)
                    .execute()

                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    val friendItems = user.map {
                        FriendItem.Friend(
                            id = it.userId,
                            name = it.name ?: "",
                            isTrackingAllowed = true
                        )
                    }.toMutableList()

                    withContext(Dispatchers.Main) {
                        friendsAdapter.updateList(friendItems)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@HomeScreen, "Failed to load users", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HomeScreen, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Location-related functions remain unchanged below...

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun promptEnableLocation() {
        if (!checkLocationPermissions()) return
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)

        val settingsClient = LocationServices.getSettingsClient(this)
        val task = settingsClient.checkLocationSettings(builder.build())
        task.addOnSuccessListener { startLocationUpdates() }
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(this, LOCATION_ENABLE_REQUEST_CODE)
                } catch (e: Exception) {
                    Log.e("HomeScreen", "Error prompting for location: ${e.message}")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (checkLocationPermissions()) {
            if (isLocationEnabled()) {
                startLocationUpdates()
            } else {
                promptEnableLocation()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        mGoogleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mGoogleMap?.isMyLocationEnabled = true
        }
        mGoogleMap?.uiSettings?.isZoomControlsEnabled = true
        mGoogleMap?.uiSettings?.isCompassEnabled = true
    }

    private fun checkLocationPermissions(): Boolean {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (checkLocationPermissions()) {
            val locationRequest = LocationRequest.create().apply {
                interval = 10000
                fastestInterval = 5000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let { location ->
                        val userLatLng = LatLng(location.latitude, location.longitude)
                        mGoogleMap?.clear()
                        val userLocationMarker = mGoogleMap?.addMarker(
                            MarkerOptions().position(userLatLng).title("Me")
                        )
                        userLocationMarker?.showInfoWindow()
                        mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 16f))
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isLocationEnabled()) {
                    startLocationUpdates()
                } else {
                    promptEnableLocation()
                }
            } else {
                Log.e("HomeScreen", "Location permission denied")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOCATION_ENABLE_REQUEST_CODE) {
            if (checkLocationPermissions()) {
                if (isLocationEnabled()) {
                    startLocationUpdates()
                } else {
                    Toast.makeText(this, "Location is still disabled", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
        private const val LOCATION_ENABLE_REQUEST_CODE = 1001
    }
}
