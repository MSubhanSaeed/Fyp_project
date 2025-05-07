package com.example.fyp_application

import android.annotation.SuppressLint
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fyp_application.databinding.ActivityIndividualScreenBinding
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


class IndividualScreen : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityIndividualScreenBinding
    private var mGoogleMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var locationPermissionGranted = false

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIndividualScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        BottomSheetBehavior.from(binding.sheet).apply {
            peekHeight = 300
            this.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        val friendItems = listOf(
            FriendItem.Friend("Subhan", true),
            FriendItem.Friend("Ali Haider", false),
            FriendItem.Friend("Kamran", true),
            FriendItem.Friend("Aminullah", true),
            FriendItem.Friend("Saad Haider", false),
            FriendItem.Friend("Arslan", false),
            FriendItem.Friend("Hashir", true),
            FriendItem.Friend("Hadi", false),
            FriendItem.AddButton
        )

        val adapter = FriendsAdapter(friendItems)
        binding.friendsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.friendsRecyclerView.adapter = adapter

        // Button clicks
        binding.btnHome.setOnClickListener {
            startActivity(Intent(this, HomeScreen::class.java))
        }
        binding.btnPlaces.setOnClickListener {
            startActivity(Intent(this, PlacesScreen::class.java))
        }
        binding.btnCircle.setOnClickListener {
            startActivity(Intent(this, CircleScreen::class.java))
        }
        binding.btnAccount.setOnClickListener {
            startActivity(Intent(this, AccountScreen::class.java))
        }

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Check permissions and setup map
        checkLocationPermissionAndSetupMap()
    }

    private fun checkLocationPermissionAndSetupMap() {
        locationPermissionGranted = checkLocationPermissions()
        if (locationPermissionGranted) {
            setupMap()
        } else {
            requestLocationPermissions()
        }
    }

    private fun setupMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this) ?: Log.e("IndividualScreen", "Map fragment is null")
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap

        // Enable UI controls
        mGoogleMap?.uiSettings?.isZoomControlsEnabled = true
        mGoogleMap?.uiSettings?.isCompassEnabled = true

        // Enable my location if permission granted
        if (locationPermissionGranted) {
            enableMyLocation()
            startLocationUpdates()
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        try {
            mGoogleMap?.isMyLocationEnabled = true
        } catch (e: SecurityException) {
            Log.e("IndividualScreen", "Location permission not granted", e)
        }
    }

    private fun checkLocationPermissions(): Boolean {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED)
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true
                    setupMap()
                } else {
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (!locationPermissionGranted) return

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
                    mGoogleMap?.addMarker(
                        MarkerOptions().position(userLatLng).title("Me")
                    )?.showInfoWindow()
                    mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 16f))
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        } catch (e: SecurityException) {
            Log.e("IndividualScreen", "Lost location permission", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (e: Exception) {
            Log.e("IndividualScreen", "Error removing location updates", e)
        }
    }
}