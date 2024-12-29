package com.example.inventory.ui.overview

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.viewModels
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inventory.R
import com.example.inventory.sms.SMSPermissionsActivity
import com.example.inventory.ui.overview.adapter.InventoryAdapter
import kotlinx.coroutines.flow.collectLatest
import com.example.inventory.data.model.Item

class OverviewActivity : AppCompatActivity() {

    private val overviewViewModel: OverviewViewModel by viewModels()

    private lateinit var addItemButton: Button
    private lateinit var settingsButton: ImageButton
    private lateinit var inventoryAdapter: InventoryAdapter
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var sortButton: ImageButton
    private lateinit var itemDetailCard: View
    private lateinit var itemDetailName: TextView
    private lateinit var itemDetailQuantity: TextView
    private lateinit var itemDetailDate: TextView
    private lateinit var itemDetailNewQuantity: EditText
    private lateinit var updateQuantityButton: Button
    private lateinit var closeCardButton: ImageButton

    companion object {
        private const val REQUEST_SMS_PERMISSION = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overview)

        addItemButton = findViewById(R.id.addItemButton)
        settingsButton = findViewById(R.id.settingsButton)
        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.searchButton)
        sortButton = findViewById(R.id.sortButton)
        // Card references
        itemDetailCard = findViewById(R.id.itemDetailCard)
        itemDetailName = findViewById(R.id.itemDetailName)
        itemDetailQuantity = findViewById(R.id.itemDetailQuantity)
        itemDetailDate = findViewById(R.id.itemDetailDate)
        itemDetailNewQuantity = findViewById(R.id.itemDetailNewQuantity)
        updateQuantityButton = findViewById(R.id.updateQuantityButton)
        closeCardButton = findViewById(R.id.closeCardButton)


        setupRecyclerView()
        setupListeners()
        observeItems()
        setupSearchFunctionality()

        sortButton.setOnClickListener {
            overviewViewModel.toggleSortOrder()
        }
        closeCardButton.setOnClickListener {
            itemDetailCard.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        inventoryAdapter = InventoryAdapter(
            onItemClicked = { item -> showItemDetails(item) },
            onQuantityUpdate = { item, newQuantity ->
                overviewViewModel.updateItemQuantity(item, newQuantity)
                if (newQuantity == 0) {
                    checkAndRequestSmsPermission(item.name)
                }
            },
            onDeleteItem = { item ->
                overviewViewModel.deleteItem(item)
            }
        )

        val recyclerView = findViewById<RecyclerView>(R.id.inventoryRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = inventoryAdapter
    }

    private fun showItemDetails(item: Item) {
        itemDetailCard.visibility = View.VISIBLE
        itemDetailName.text = item.name
        itemDetailQuantity.text = "Quantity: ${item.quantity}"
        itemDetailDate.text = "Date Added: ${item.dateAdded}"

        updateQuantityButton.setOnClickListener {
            val newQuantity = itemDetailNewQuantity.text.toString().toIntOrNull()
            if (newQuantity != null && newQuantity >= 0) {
                overviewViewModel.updateItemQuantity(item, newQuantity)
                Toast.makeText(this, "Quantity updated", Toast.LENGTH_SHORT).show()
                itemDetailCard.visibility = View.GONE
            } else {
                Toast.makeText(this, "Invalid quantity", Toast.LENGTH_SHORT).show()
            }
        }
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

    private fun setupSearchFunctionality() {
        searchButton.setOnClickListener {
            val query = searchEditText.text.toString().lowercase().trim()
            if (query.isNotEmpty()) {
                performSearch(query)
            } else {
                resetSearch()
            }
        }
    }

    private fun performSearch(query: String) {
        val searchResults = overviewViewModel.searchItems(query)
        if (searchResults.isNotEmpty()) {
            inventoryAdapter.submitList(searchResults)
        } else {
            Toast.makeText(this, "No matches found for \"$query\"", Toast.LENGTH_SHORT).show()
        }
    }


    private fun resetSearch() {
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
