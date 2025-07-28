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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fyp_application.Adapters.IndividualFriendAdapter
import com.example.fyp_application.HomeScreen.Companion.LOCATION_PERMISSION_REQUEST_CODE
import com.example.fyp_application.databinding.ActivityIndividualScreenBinding
import com.example.fyp_application.dto.IndividualLiveLocationEntity
import com.example.fyp_application.model.IndFriend
import com.example.fyp_application.network.RetrofitClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class IndividualScreen : AppCompatActivity(), OnMapReadyCallback {

    private var mGoogleMap: GoogleMap? = null
    private lateinit var binding: ActivityIndividualScreenBinding
    private lateinit var individualAdapter: IndividualFriendAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var myLocationMarker: Marker? = null
    private val friendMarkers = mutableMapOf<Int, Marker>()


    private val addFriendLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            refreshData()
        }
    }
    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun refreshData() {
        fetchFriendsData() // Refresh friend list
        loadLiveFriendLocations() // Refresh locations
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIndividualScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomSheet()
        setupBottomNavigation()
        initializeAdapter()
        fetchFriendsData()
        setupLocationServices()
        setupMap()

        binding.btnAddFriend.setOnClickListener {
            addFriendLauncher.launch(Intent(this, AddFriend::class.java))
        }

    }
    companion object {
        private const val LIVE_LOCATION_UPDATE_INTERVAL = 10000L
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap

        if (checkLocationPermissions()) mGoogleMap?.isMyLocationEnabled = true

        mGoogleMap?.uiSettings?.apply {
            isZoomControlsEnabled = true
            isMyLocationButtonEnabled = true
            isCompassEnabled = true
        }

        // 🔹 NEW: animate to marker when it’s tapped
        mGoogleMap?.setOnMarkerClickListener { marker ->
            if (marker != myLocationMarker) {
                mGoogleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.position, 16f))
                marker.showInfoWindow()
            }
            true   // event handled
        }

        if (checkLocationPermissions() && isLocationEnabled()) startLocationUpdates()

        loadLiveFriendLocations()
    }

    private fun initializeAdapter() {
        individualAdapter = IndividualFriendAdapter(mutableListOf()) { friendId, isChecked ->
            Toast.makeText(
                this,
                "Tracking for friend $friendId ${if (isChecked) "enabled" else "disabled"}",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.indfriendsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@IndividualScreen)
            adapter = individualAdapter
        }
    }

    private fun setupBottomSheet() {
        BottomSheetBehavior.from(binding.sheet).apply {
            peekHeight = 500
            state = BottomSheetBehavior.STATE_COLLAPSED
        }
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

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.btnIndividual
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.btnHome -> {
                    startActivity(Intent(this, HomeScreen::class.java))
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
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                        )
                    } else {
                        myLocationMarker?.apply { position = userLatLng }
                    }

                    myLocationMarker?.showInfoWindow()
                    mGoogleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 16f))
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun fetchFriendsData() {
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userId = sharedPref.getInt("userid", -1).takeIf { it != -1 } ?: run {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.getInstance().getUserApiService()
            .getFriendsdetails(userId)
            .enqueue(object : Callback<List<IndividualLiveLocationEntity>> {
                override fun onResponse(
                    call: Call<List<IndividualLiveLocationEntity>>,
                    response: Response<List<IndividualLiveLocationEntity>>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { friendsList ->
                            val indFriends = friendsList.map { friend ->
                                IndFriend(
                                    id = friend.id,
                                    name = friend.name ?: "Unknown",
                                    friendshipStatus = friend.friendshipStatus ?: "Unknown",
                                    permission = friend.permission ?: "false"
                                )
                            }
                            individualAdapter.updateList(indFriends)
                        }
                    } else {
                        Toast.makeText(this@IndividualScreen, "Failed to load friends", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<IndividualLiveLocationEntity>>, t: Throwable) {
                    Toast.makeText(this@IndividualScreen, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateFriendMarkers(locations: List<IndividualLiveLocationEntity>) {
        friendMarkers.values.forEach { if (it != myLocationMarker) it.remove() }
        friendMarkers.clear()

        locations.forEach { loc ->
            val lat = loc.latitude ?: return@forEach
            val lng = loc.longitude ?: return@forEach
            val latLng = LatLng(lat.toDouble(), lng.toDouble())
            val markerOptions = MarkerOptions()
                .position(latLng)
                .title(loc.name ?: "Friend")
                .icon(getInitialIcon(loc.name ?: "?"))

            mGoogleMap?.addMarker(markerOptions)?.let { marker ->
                friendMarkers[loc.id] = marker
            }
        }

        if (locations.isNotEmpty() || myLocationMarker != null) {
            val bounds = LatLngBounds.Builder()
            locations.forEach { b -> b.latitude?.let { lat ->
                b.longitude?.let { lng -> bounds.include(LatLng(lat.toDouble(), lng.toDouble())) }
            }}
            myLocationMarker?.position?.let { bounds.include(it) }
            try { mGoogleMap?.animateCamera(
                CameraUpdateFactory.newLatLngBounds(bounds.build(), 100))
            } catch (_: Exception) { /* ignore if bounds invalid */ }
        }
    }


    private fun loadLiveFriendLocations() {
        val userId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getInt("userid", -1)
        if (userId == -1) return

        RetrofitClient.getInstance().getUserApiService()
            .GetUserFriendsLiveLocation(userId)
            .enqueue(object : Callback<List<IndividualLiveLocationEntity>> {
                override fun onResponse(
                    call: Call<List<IndividualLiveLocationEntity>>,
                    resp: Response<List<IndividualLiveLocationEntity>>
                ) {
                    if (resp.isSuccessful && resp.body() != null) {
                        updateFriendMarkers(resp.body()!!)
                    } else {
                        Toast.makeText(this@IndividualScreen,
                            "No live locations found", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<List<IndividualLiveLocationEntity>>, t: Throwable) =
                    Toast.makeText(this@IndividualScreen,
                        "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            })
    }

    private fun getInitialIcon(name: String): BitmapDescriptor {
        val initial = name.trim().takeIf { it.isNotEmpty() }?.get(0)?.uppercaseChar().toString()
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val baseColor = generateColorFromName(name)

        val paintCircle = Paint().apply {
            color = baseColor
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val isDark = (Color.red(baseColor) * 0.299 + Color.green(baseColor) * 0.587 + Color.blue(baseColor) * 0.114) < 186
        val paintText = Paint().apply {
            color = if (isDark) Color.WHITE else Color.BLACK
            textSize = 40f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        canvas.drawCircle(50f, 50f, 50f, paintCircle)
        canvas.drawText(initial, 50f, 65f, paintText)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun generateColorFromName(name: String): Int {
        val hash = name.hashCode()
        val r = 100 + (hash shr 16 and 0x7F)
        val g = 100 + (hash shr 8 and 0x7F)
        val b = 100 + (hash and 0x7F)
        return Color.rgb(r, g, b)
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
