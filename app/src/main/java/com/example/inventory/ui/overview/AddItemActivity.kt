package com.example.inventory.ui.overview

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.inventory.R
import com.example.inventory.data.model.Item
import java.text.SimpleDateFormat
import java.util.*

/**
 * AddItemActivity allows the user to add a new inventory item and save it to the database.
 */
class AddItemActivity : AppCompatActivity() {

    private val overviewViewModel: OverviewViewModel by viewModels() // ViewModel for managing items

    // UI elements
    private lateinit var itemNameEditText: EditText
    private lateinit var itemQuantityEditText: EditText
    private lateinit var saveItemButton: Button
    private lateinit var returnButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_additem)

        // Initialize UI elements
        itemNameEditText = findViewById(R.id.itemNameEditText)
        itemQuantityEditText = findViewById(R.id.itemQuantityEditText)
        saveItemButton = findViewById(R.id.saveItemButton)
        returnButton = findViewById(R.id.returnButton)

        // Set the button listeners
        saveItemButton.setOnClickListener {
            addItemToInventory()
        }
        returnButton.setOnClickListener {
            finish()
        }
    }

    /**
     * Validates input fields and adds a new item to the inventory via the ViewModel.
     */
    private fun addItemToInventory() {
        val itemName = itemNameEditText.text.toString().trim()
        val quantityStr = itemQuantityEditText.text.toString().trim()

        // Validate inputs
        if (itemName.isBlank()) {
            Toast.makeText(this, "Item name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val quantity = quantityStr.toIntOrNull()
        if (quantity == null || quantity < 0) {
            Toast.makeText(this, "Please enter a valid quantity", Toast.LENGTH_SHORT).show()
            return
        }

        // Format the current date
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Create or update the item
        val newItem = Item(
            id = UUID.randomUUID().toString(), // Generate Firestore-compatible ID
            name = itemName,
            quantity = quantity,
            dateAdded = currentDate
        )

        overviewViewModel.addOrUpdateItem(newItem) // Add item via ViewModel
        Toast.makeText(this, "Item added successfully", Toast.LENGTH_SHORT).show()
        finish()
    }
}
