package com.example.fyp_application

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.lifecycleScope
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fyp_application.Adapters.HomeAdapter
import com.example.fyp_application.databinding.ActivityHomeScreenBinding
import com.example.fyp_application.dto.UserLiveLocationEntity
import com.example.fyp_application.model.FriendItem
import com.example.fyp_application.response.GroupResponse
import com.example.fyp_application.network.RetrofitClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.*

class HomeScreen : AppCompatActivity(), OnMapReadyCallback {

    private var mGoogleMap: GoogleMap? = null
    private lateinit var binding: ActivityHomeScreenBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private val groups = mutableListOf<GroupResponse>()
    private lateinit var spinnerAdapter: ArrayAdapter<String>
    private lateinit var friendsAdapter: HomeAdapter
    private val friendMarkers = mutableMapOf<Int, Marker>()
    private var currentGroupId: Int = -1
    private var locationUpdateJob: Job? = null
    private var myLocationMarker: Marker? = null

    companion object {
        internal const val LOCATION_PERMISSION_REQUEST_CODE = 1000
        private const val LOCATION_ENABLE_REQUEST_CODE = 1001
        public const val LIVE_LOCATION_UPDATE_INTERVAL = 20000L
        public const val TAG = "HomeScreen"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setupUI()
        setupLocationServices()
        fetchGroupsFromApi()
    }

    private fun setupUI() {
        setupBottomSheet()
        setupRecyclerView()
        setupBottomNavigation()
        setupMap()
        setupSpinner()
    }

    private fun setupBottomSheet() {
        BottomSheetBehavior.from(binding.sheet).apply {
            peekHeight = 500
            state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        binding.friendsRecyclerView.layoutManager = layoutManager

        friendsAdapter = HomeAdapter(mutableListOf()) { friendId, friendName ->
            // 1. Center map on friend's location (if marker exists)
            friendMarkers[friendId]?.let { marker ->
                mGoogleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.position, 16f))
                marker.showInfoWindow()
            }

            // 2. Navigate to UserTripActivity
            val intent = Intent(this, UserTripActivity::class.java).apply {
                putExtra("FRIEND_ID", friendId)
                putExtra("FRIEND_NAME", friendName)
                // Add any other data you want to pass
            }
            startActivity(intent)
        }

        binding.friendsRecyclerView.adapter = friendsAdapter
    }


    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.btnHome
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.btnIndividual -> {
                    startActivity(Intent(this, IndividualScreen::class.java))
                    true
                }
                R.id.btnPlaces -> {
                    startActivity(Intent(this, PlacesScreen::class.java))
                    true
                }
                R.id.btnCircle -> {
                    startActivity(Intent(this, CircleScreen::class.java))
                    true
                }
                R.id.btnAccount -> {
                    startActivity(Intent(this, AccountScreen::class.java))
                    true
                }
                else -> false
            }
        }
    }


    private fun setupMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
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

                // Clear old markers before loading new group members
                clearMapLocation()

                // Fetch and show users in the selected group
                fetchUsersInGroup(selectedGroup.groupId)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun clearMapLocation() {
        friendMarkers.values.forEach { marker ->
            marker.remove()
        }
        friendMarkers.clear()
    }


    private fun setupLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
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

    private fun fetchGroupsFromApi() {
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userId = sharedPref.getInt("userid", -1)

        lifecycleScope.launch(Dispatchers.IO) {
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
                        Toast.makeText(
                            this@HomeScreen,
                            "Failed to load groups: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HomeScreen, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    Log.e("HomeScreen", "API error", e)
                }
            }
        }
    }


    private fun fetchUsersInGroup(groupId: Int) {
        currentGroupId = groupId

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val usersResponse = RetrofitClient.getInstance()
                    .getUserApiService()
                    .getUsersInGroup(groupId)
                    .execute()

                if (usersResponse.isSuccessful && usersResponse.body() != null) {
                    val users = usersResponse.body()!!
                    val friendItems = users.map {
                        FriendItem.Friend(it.userId, it.name ?: "Unknown", true)
                    }.toMutableList()

                    withContext(Dispatchers.Main) {
                        friendsAdapter.updateList(friendItems)
                    }

                    try {
                        fetchLiveLocations(groupId)
                    } catch (e: Exception) {
                        Log.e(TAG, "Live location fetch failed: ${e.message}")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@HomeScreen, "Failed to load group members", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HomeScreen, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun fetchLiveLocations(groupId: Int) {
        val locationsResponse = RetrofitClient.getInstance()
            .getUserApiService()
            .getGroupFriendsLiveLocation(groupId)
            .execute()

        if (locationsResponse.isSuccessful && locationsResponse.body() != null) {
            val locations = locationsResponse.body()!!
            withContext(Dispatchers.Main) {
                updateFriendMarkers(locations)
                startLiveLocationUpdates(groupId)
            }
        } else {
            Log.w(TAG, "Live locations not available (${locationsResponse.code()})")
        }
    }

    private fun startLiveLocationUpdates(groupId: Int) {
        locationUpdateJob?.cancel()
        locationUpdateJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive && currentGroupId == groupId) {
                try {
                    val response = RetrofitClient.getInstance()
                        .getUserApiService()
                        .getGroupFriendsLiveLocation(groupId)
                        .execute()

                    if (response.isSuccessful && response.body() != null) {
                        val liveLocations = response.body()!!
                        withContext(Dispatchers.Main) {
                            updateFriendMarkers(liveLocations)
                        }
                    }

                    delay(LIVE_LOCATION_UPDATE_INTERVAL)
                } catch (e: Exception) {
                    Log.e(TAG, "Live location update error: ${e.message}")
                    delay(8000)
                }
            }
        }
    }

    private fun updateFriendMarkers(locations: List<UserLiveLocationEntity>) {
        friendMarkers.values.forEach { if (it != myLocationMarker) it.remove() }
        friendMarkers.clear()

        locations.forEach { location ->
            val latLng = LatLng(location.latitude, location.longitude)
            val markerOptions = MarkerOptions()
                .position(latLng)
                .title(location.name)
                .snippet("Updated: ${location.time}")
                .icon(getInitialIcon(location.name))

            mGoogleMap?.addMarker(markerOptions)?.let { marker ->
                friendMarkers[location.userId] = marker
            }
        }

        if (locations.isNotEmpty() || myLocationMarker != null) {
            val bounds = LatLngBounds.Builder()
            locations.forEach { bounds.include(LatLng(it.latitude, it.longitude)) }
            myLocationMarker?.position?.let { bounds.include(it) }

            try {
                mGoogleMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100))
            } catch (e: Exception) {
                Log.e(TAG, "Camera update error: ${e.message}")
            }
        }
    }

    private fun getInitialIcon(name: String): BitmapDescriptor {
        val initial = if (name.isNotEmpty()) name[0].uppercaseChar().toString() else "?"
        var color = getColorForInitial(initial)

        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        Paint().apply {
            this.color = color
            style = Paint.Style.FILL
            isAntiAlias = true
        }.let { canvas.drawCircle(50f, 50f, 50f, it) }

        Paint().apply {
            color = Color.WHITE
            textSize = 40f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }.let { canvas.drawText(initial, 50f, 65f, it) }

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    // Define this map once, outside the function, to keep it persistent across calls
    private val initialColorMap = mutableMapOf<String, Int>()

    private fun getColorForInitial(name: String): Int {
        val colors = listOf(
            Color.parseColor("#4285F4"),  // Blue
            Color.parseColor("#EA4335"),  // Red
            Color.parseColor("#FBBC05"),  // Yellow
            Color.parseColor("#34A853"),  // Green
            Color.parseColor("#673AB7"),  // Purple
            Color.parseColor("#FF5722"),  // Orange
            Color.parseColor("#00BCD4"),  // Cyan
            Color.parseColor("#E91E63"),  // Pink
            Color.parseColor("#8BC34A"),  // Light Green
            Color.parseColor("#795548")   // Brown
        )

        // Return the assigned color if already mapped
        if (initialColorMap.containsKey(name)) {
            return initialColorMap[name]!!
        }

        // Assign next available color in a rotating manner
        val colorIndex = initialColorMap.size % colors.size
        val color = colors[colorIndex]

        initialColorMap[name] = color
        return color
    }




    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap.apply {
            mapType = GoogleMap.MAP_TYPE_NORMAL
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isCompassEnabled = true

            setOnMarkerClickListener { marker ->
                if (marker != myLocationMarker) {
                    animateCamera(CameraUpdateFactory.newLatLngZoom(marker.position, 16f))
                    marker.showInfoWindow()
                }
                true
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mGoogleMap?.isMyLocationEnabled = true
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (!checkLocationPermissions()) return

        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val userLatLng = LatLng(location.latitude, location.longitude)

                    myLocationMarker = if (myLocationMarker == null) {
                        mGoogleMap?.addMarker(
                            MarkerOptions()
                                .position(userLatLng)
                                .title("Me")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        )
                    } else {
                        myLocationMarker?.apply { position = userLatLng }
                    }

                    myLocationMarker?.showInfoWindow()

                    if (currentGroupId == -1) {
                        mGoogleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 16f))
                    }
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun checkLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun promptEnableLocation() {
        Toast.makeText(this, "Please enable location services", Toast.LENGTH_LONG).show()
        startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                if (isLocationEnabled()) {
                    startLocationUpdates()
                } else {
                    promptEnableLocation()
                }
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (checkLocationPermissions() && isLocationEnabled()) {
            startLocationUpdates()
        }
        if (currentGroupId != -1) {
            startLiveLocationUpdates(currentGroupId)
        }
    }

    override fun onPause() {
        super.onPause()
        locationUpdateJob?.cancel()

        // Prevent UninitializedPropertyAccessException
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

}