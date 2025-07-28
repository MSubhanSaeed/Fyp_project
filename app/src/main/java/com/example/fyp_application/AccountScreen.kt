package com.example.fyp_application

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.fyp_application.databinding.ActivityAccountScreenBinding

class AccountScreen : AppCompatActivity() {
    private lateinit var binding: ActivityAccountScreenBinding
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private val permissionRequestLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            showToast("Bluetooth permission is required")
        }
    }

    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        updateBluetoothSwitch()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAccountScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupBluetoothFunctionality()
    }

    private fun setupNavigation() {
        binding.editProfileOption.setOnClickListener {
            startActivity(Intent(this, EditProfile_Screen::class.java))
        }
        binding.Notifications.setOnClickListener {
            startActivity(Intent(this, NotificationScreen::class.java))
        }

        binding.leavecircle.setOnClickListener {
            showLeaveCircleDialog()
        }

        binding.logoutContainer.setOnClickListener {
            getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().clear().apply()
            val intent = Intent(this, LoginScreen::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.bottomNavigation.selectedItemId = R.id.btnAccount

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
                R.id.btnHome -> {
                    startActivity(Intent(this, HomeScreen::class.java))
                    true
                }
                else -> false
            }
        }


    }


    private fun setupBluetoothFunctionality() {
        binding.bluetoothSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (bluetoothAdapter == null) {
                showToast("Bluetooth not supported")
                binding.bluetoothSwitch.isChecked = false
                return@setOnCheckedChangeListener
            }

            if (isChecked) {
                if (!bluetoothAdapter.isEnabled) {
                    val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    enableBluetoothLauncher.launch(enableIntent)
                }
            } else {
                if (checkBluetoothPermission()) {
                    bluetoothAdapter.disable()
                    showToast("Bluetooth turned off")
                } else {
                    requestBluetoothPermission()
                }
            }
        }

        binding.bluetoothBox.setOnClickListener {
            if (bluetoothAdapter == null) {
                showToast("Bluetooth not supported")
                return@setOnClickListener
            }

            if (!checkBluetoothPermission()) {
                requestBluetoothPermission()
                return@setOnClickListener
            }

            if (!bluetoothAdapter.isEnabled) {
                showToast("Please enable Bluetooth first")
                return@setOnClickListener
            }

            val pairedDevices = bluetoothAdapter.bondedDevices
            if (!pairedDevices.isNullOrEmpty()) {
                val deviceNames = pairedDevices.map { "${it.name} (${it.address})" }.toTypedArray()

                AlertDialog.Builder(this)
                    .setTitle("Paired Bluetooth Devices")
                    .setItems(deviceNames) { _, which ->
                        showToast("Selected: ${deviceNames[which]}")
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            } else {
                showToast("No paired devices found")
            }
        }
    }

    private fun updateBluetoothSwitch() {
        if (bluetoothAdapter != null && checkBluetoothPermission()) {
            binding.bluetoothSwitch.isChecked = bluetoothAdapter.isEnabled
        }
    }

    private fun checkBluetoothPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestBluetoothPermission() {
        permissionRequestLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
    }

    private fun showLeaveCircleDialog() {
        AlertDialog.Builder(this)
            .setMessage("You will no longer see or share locations with this circle. Are you sure you want to leave?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                showToast("You have left the circle.")
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    // 🔧 Helper function to avoid repeating Toast code
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
