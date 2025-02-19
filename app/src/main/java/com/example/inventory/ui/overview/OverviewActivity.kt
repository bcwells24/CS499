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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inventory.R
import com.example.inventory.sms.SMSPermissionsActivity
import com.example.inventory.ui.overview.adapter.InventoryAdapter
import kotlinx.coroutines.flow.collectLatest
import com.example.inventory.data.model.Item

/**
 * OverviewActivity displays the inventory list and allows users to perform a variety of operations:
 * - Adding new items
 * - Updating item quantity
 * - Sorting and filtering items
 * - Navigating to the SMS permissions screen
 */
class OverviewActivity : AppCompatActivity() {

    // Obtain the ViewModel that manages the inventory data
    private val overviewViewModel: OverviewViewModel by viewModels()

    // UI elements
    private lateinit var addItemButton: Button
    private lateinit var settingsButton: ImageButton
    private lateinit var inventoryAdapter: InventoryAdapter
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var sortButton: ImageButton

    // UI elements for displaying and updating the details of a selected item
    private lateinit var itemDetailCard: View
    private lateinit var itemDetailName: TextView
    private lateinit var itemDetailQuantity: TextView
    private lateinit var itemDetailDate: TextView
    private lateinit var itemDetailNewQuantity: EditText
    private lateinit var updateQuantityButton: Button
    private lateinit var closeCardButton: ImageButton

    companion object {
        private const val REQUEST_SMS_PERMISSION = 123 // Used for requesting SMS permission
    }

    /**
     * Called when the activity is created. Sets up UI elements, configures the RecyclerView,
     * and observes changes to the inventory data.
     *
     * @param savedInstanceState Bundle containing any saved instance state if the system
     *                           is recreating the activity (e.g., on configuration change).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overview)

        // Initialize UI elements
        addItemButton = findViewById(R.id.addItemButton)
        settingsButton = findViewById(R.id.settingsButton)
        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.searchButton)
        sortButton = findViewById(R.id.sortButton)
        itemDetailCard = findViewById(R.id.itemDetailCard)
        itemDetailName = findViewById(R.id.itemDetailName)
        itemDetailQuantity = findViewById(R.id.itemDetailQuantity)
        itemDetailDate = findViewById(R.id.itemDetailDate)
        itemDetailNewQuantity = findViewById(R.id.itemDetailNewQuantity)
        updateQuantityButton = findViewById(R.id.updateQuantityButton)
        closeCardButton = findViewById(R.id.closeCardButton)

        // Setup the inventory list
        setupRecyclerView()
        // Setup main UI interactions like adding items or opening the settings screen
        setupListeners()
        // Observe items from the ViewModel in real-time
        observeItems()
        // Initialize the search functionality (search and reset)
        setupSearchFunctionality()

        // Listen for sort button clicks
        sortButton.setOnClickListener {
            overviewViewModel.toggleSortOrder()
        }

        // Close the item detail card when the close button is pressed
        closeCardButton.setOnClickListener {
            itemDetailCard.visibility = View.GONE
        }
    }

    /**
     * Configures the RecyclerView with an InventoryAdapter and sets up item interaction callbacks.
     */
    private fun setupRecyclerView() {
        inventoryAdapter = InventoryAdapter(
            onItemClicked = { item -> showItemDetails(item) },
            onQuantityUpdate = { item, newQuantity ->
                // Update item quantity via the ViewModel
                overviewViewModel.updateItemQuantity(item, newQuantity)

                // If the new quantity is zero, check permissions to possibly send an SMS alert
                if (newQuantity == 0) {
                    checkAndRequestSmsPermission(item.name)
                }
            },
            onDeleteItem = { item ->
                // Remove the item from the inventory
                overviewViewModel.deleteItem(item)
            }
        )

        val recyclerView = findViewById<RecyclerView>(R.id.inventoryRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = inventoryAdapter
    }

    /**
     * Displays the details of the selected item in a card layout, allowing the user
     * to manually change the item’s quantity.
     *
     * @param item The inventory item that was selected.
     */
    private fun showItemDetails(item: Item) {
        // Make the detail card visible
        itemDetailCard.visibility = View.VISIBLE

        // Populate the detail card fields
        itemDetailName.text = item.name
        itemDetailQuantity.text = getString(R.string.quantity_text, item.quantity)
        itemDetailDate.text = getString(R.string.date_added_text, item.dateAdded)

        // Update the item's quantity when the button is pressed
        updateQuantityButton.setOnClickListener {
            val newQuantity = itemDetailNewQuantity.text.toString().toIntOrNull()
            if (newQuantity != null && newQuantity >= 0) {
                overviewViewModel.updateItemQuantity(item, newQuantity)
                Toast.makeText(this, "Quantity updated", Toast.LENGTH_SHORT).show()
                // Hide the detail card after updating
                itemDetailCard.visibility = View.GONE
            } else {
                Toast.makeText(this, "Invalid quantity", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Sets up click listeners for adding new items and opening the settings screen.
     */
    private fun setupListeners() {
        // Navigate to the AddItemActivity
        addItemButton.setOnClickListener {
            startActivity(Intent(this, AddItemActivity::class.java))
        }

        // Navigate to the SMSPermissionsActivity
        settingsButton.setOnClickListener {
            startActivity(Intent(this, SMSPermissionsActivity::class.java))
        }
    }

    /**
     * Observes changes to the inventory data through a Flow, updating the adapter
     * whenever new data is emitted.
     */
    private fun observeItems() {
        lifecycleScope.launch {
            // Collect from the Flow when in STARTED state
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                overviewViewModel.allItems.collectLatest { items ->
                    // Update the RecyclerView's list of items
                    inventoryAdapter.submitList(items)
                }
            }
        }
    }

    /**
     * Sets up the search button to filter the inventory list, and also provides a way
     * to reset the list if the search query is empty.
     */
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

    /**
     * Filters items based on a user-provided search query.
     *
     * @param query The string to search for in item names.
     */
    private fun performSearch(query: String) {
        // Ask the ViewModel to filter the item list
        val searchResults = overviewViewModel.searchItems(query)
        if (searchResults.isNotEmpty()) {
            inventoryAdapter.submitList(searchResults)
        } else {
            Toast.makeText(this, "No matches found for \"$query\"", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Resets the inventory list to show all items when no search query is provided.
     */
    private fun resetSearch() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                overviewViewModel.allItems.collectLatest { items ->
                    inventoryAdapter.submitList(items)
                }
            }
        }
    }

    /**
     * Checks if the app has permission to send SMS. If not, requests the permission from the user.
     * If already granted, triggers sending an SMS notification about the zero quantity item.
     *
     * @param itemName The name of the item that triggered the SMS alert.
     */
    fun checkAndRequestSmsPermission(itemName: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED) {
            // Request the SMS permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.SEND_SMS),
                REQUEST_SMS_PERMISSION
            )
        } else {
            // Permission granted, proceed with sending an SMS via the ViewModel
            overviewViewModel.sendZeroQuantitySms(itemName)
        }
    }

    /**
     * Handles the callback for the SMS permission request.
     *
     * @param requestCode  Unique code identifying the permission request.
     * @param permissions  The requested permissions.
     * @param grantResults The results of each requested permission.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_SMS_PERMISSION && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "SMS Permission Granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "SMS Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }
}

/** Reference: https://developer.android.com/reference/android/app/Activity.html?hl=en
 * Created by: Bradley Wells*/
