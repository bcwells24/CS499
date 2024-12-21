package com.example.inventory

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class OverviewActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_SMS_PERMISSION = 123
        private const val PREFS_NAME = "SMSPrefs"
        private const val KEY_SMS_PERMISSION = "sms_permission"
    }

    private lateinit var inventoryRecyclerView: RecyclerView
    private lateinit var inventoryAdapter: InventoryAdapter
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var itemList: MutableList<Item>
    private lateinit var addItemButton: Button
    private lateinit var settingsButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overview)

        initializeViews()
        setupRecyclerView()
        setupListeners()
    }

    private fun initializeViews() {
        inventoryRecyclerView = findViewById(R.id.inventoryRecyclerView)
        addItemButton = findViewById(R.id.addItemButton)
        settingsButton = findViewById(R.id.settingsButton)
    }

    private fun setupRecyclerView() {
        databaseHelper = DatabaseHelper(this)
        itemList = mutableListOf()
        inventoryAdapter = InventoryAdapter(itemList, this, databaseHelper)
        inventoryRecyclerView.layoutManager = LinearLayoutManager(this)
        inventoryRecyclerView.adapter = inventoryAdapter
    }

    private fun setupListeners() {
        addItemButton.setOnClickListener {
            startActivity(Intent(this, AddItemActivity::class.java))
        }

        settingsButton.setOnClickListener {
            startActivity(Intent(this, SMSPermissionsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadItems()
        checkSmsPermissionState()
    }

    private fun checkSmsPermissionState() {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val isSmsEnabled = sharedPreferences.getBoolean(KEY_SMS_PERMISSION, false)
        inventoryAdapter.setSmsEnabled(isSmsEnabled)

        if (!isSmsEnabled) {
            showSmsPermissionDialog()
        }
    }

    private fun loadItems() {
        itemList.clear()
        itemList.addAll(databaseHelper.getAllItems())
        inventoryAdapter.notifyDataSetChanged()
    }

    fun checkAndRequestSmsPermission(itemName: String, phoneNumber: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), REQUEST_SMS_PERMISSION)
        } else {
            sendSmsNotification(phoneNumber, itemName)
        }
    }

    private fun showSmsPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Enable SMS Notifications")
            .setMessage("To receive alerts when an item reaches zero quantity, please enable SMS notifications in the settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                startActivity(Intent(this, SMSPermissionsActivity::class.java))
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun sendSmsNotification(phoneNumber: String, itemName: String) {
        if (phoneNumber.isNotEmpty()) {
            val message = "Item: $itemName has reached zero quantity."
            val success = SMSManager.sendSMS(phoneNumber, message)

            if (success) {
                Toast.makeText(this, "SMS sent successfully.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to send SMS.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
