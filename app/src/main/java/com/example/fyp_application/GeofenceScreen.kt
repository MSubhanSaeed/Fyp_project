package com.example.fyp_application


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fyp_application.databinding.ActivityGeofenceScreenBinding
import com.example.fyp_application.model.GeoFriend

class GeofenceScreen : AppCompatActivity() {

    private lateinit var binding: ActivityGeofenceScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGeofenceScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
}