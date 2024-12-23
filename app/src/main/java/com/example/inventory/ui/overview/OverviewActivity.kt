package com.example.inventory.ui.overview

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inventory.R
import com.example.inventory.sms.SMSPermissionsActivity
import com.example.inventory.ui.overview.adapter.InventoryAdapter
import kotlinx.coroutines.flow.collectLatest

class OverviewActivity : AppCompatActivity() {

    private val overviewViewModel: OverviewViewModel by viewModels()

    private lateinit var addItemButton: Button
    private lateinit var settingsButton: ImageButton
    private lateinit var inventoryAdapter: InventoryAdapter

    companion object {
        private const val REQUEST_SMS_PERMISSION = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overview)

        addItemButton = findViewById(R.id.addItemButton)
        settingsButton = findViewById(R.id.settingsButton)

        setupRecyclerView()
        setupListeners()
        observeItems()
    }

    private fun setupRecyclerView() {
        inventoryAdapter = InventoryAdapter { item, newQuantity ->
            overviewViewModel.updateItemQuantity(item, newQuantity)
            if (newQuantity == 0) checkAndRequestSmsPermission(item.name)
        }
        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.inventoryRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = inventoryAdapter
    }

    private fun setupListeners() {
        addItemButton.setOnClickListener {
            startActivity(Intent(this, AddItemActivity::class.java))
        }

        settingsButton.setOnClickListener {
            startActivity(Intent(this, SMSPermissionsActivity::class.java))
        }
    }

    private fun observeItems() {
        lifecycleScope.launchWhenStarted {
            overviewViewModel.allItems.collectLatest { items ->
                inventoryAdapter.submitList(items)
            }
        }
    }

    fun checkAndRequestSmsPermission(itemName: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), REQUEST_SMS_PERMISSION)
        } else {
            overviewViewModel.sendZeroQuantitySms(itemName)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_SMS_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // If user grants SMS permission after prompt, re-check or proceed with sending SMS
            Toast.makeText(this, "SMS Permission Granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "SMS Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }
}
