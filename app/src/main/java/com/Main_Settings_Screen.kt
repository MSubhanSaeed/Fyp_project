package com.example.fyp_application

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.fyp_application.*
import com.example.fyp_application.databinding.ActivityMainSettingsScreenBinding

class Main_Settings_Screen : AppCompatActivity() {
    private lateinit var binding: ActivityMainSettingsScreenBinding
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
        binding = ActivityMainSettingsScreenBinding.inflate(layoutInflater)
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

        binding.logoutContainer.setOnClickListener {
            startActivity(Intent(this, LoginScreen::class.java))
            finish()
        }

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
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
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    bluetoothAdapter.disable()
                    Toast.makeText(this, "Bluetooth turned off", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permission required to turn off Bluetooth", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.bluetoothBox.setOnClickListener {
            if (bluetoothAdapter == null) {
                Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Bluetooth permission not granted", Toast.LENGTH_SHORT).show()
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
