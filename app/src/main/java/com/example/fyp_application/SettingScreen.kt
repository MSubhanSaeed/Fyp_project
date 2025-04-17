package com.example.fyp_application

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.fyp_application.databinding.ActivitySettingScreenBinding

class SettingScreen : AppCompatActivity() {
    private lateinit var binding: ActivitySettingScreenBinding
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private val enableBluetoothLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (bluetoothAdapter?.isEnabled == true) {
            Toast.makeText(this, "Bluetooth enabled", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupBluetoothFunctionality()
    }

    private fun setupNavigation() {
        binding.editProfileOption.setOnClickListener {
            startActivity(Intent(this, EditProfile_Screen::class.java))
        }

        binding.circleOption.setOnClickListener {
            startActivity(Intent(this, CircleScreen::class.java))
        }

        binding.creategrp.setOnClickListener {
            startActivity(Intent(this, CreateGroupScreen::class.java))
        }

        binding.logoutContainer.setOnClickListener {
            startActivity(Intent(this, LoginScreen::class.java))
            finish()
        }

        binding.settingsIcon.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.Role.setOnClickListener {
            startActivity(Intent(this, RoleScreen::class.java))
        }

        binding.leavecircle.setOnClickListener {
            showLeaveCircleDialog()
        }
    }

    private fun showLeaveCircleDialog() {
        AlertDialog.Builder(this)
            .setMessage("You will no longer see or share locations with this circle. Are you sure you want to leave?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                Toast.makeText(this, "You have left the circle.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun setupBluetoothFunctionality() {
        binding.bluetoothSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (bluetoothAdapter == null) {
                Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show()
                return@setOnCheckedChangeListener
            }

            if (isChecked) {
                if (!bluetoothAdapter.isEnabled) {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    enableBluetoothLauncher.launch(enableBtIntent)
                }
            } else {
                bluetoothAdapter.disable()
                Toast.makeText(this, "Bluetooth turned off", Toast.LENGTH_SHORT).show()
            }
        }

        binding.bluetoothBox.setOnClickListener {
            if (bluetoothAdapter == null) {
                Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!bluetoothAdapter.isEnabled) {
                Toast.makeText(this, "Please enable Bluetooth first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
            if (!pairedDevices.isNullOrEmpty()) {
                val deviceNames = pairedDevices.map { "${it.name} (${it.address})" }.toTypedArray()

                AlertDialog.Builder(this)
                    .setTitle("Paired Bluetooth Devices")
                    .setItems(deviceNames) { _, which ->
                        Toast.makeText(this, "Clicked: ${deviceNames[which]}", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            } else {
                Toast.makeText(this, "No paired devices found", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
