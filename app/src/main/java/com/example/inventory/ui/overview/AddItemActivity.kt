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
 * AddItemActivity allows the user to input details for a new inventory item and save it
 * to the database via the ViewModel. It validates the provided data before creating an Item object.
 */
class AddItemActivity : AppCompatActivity() {

    // Obtain a reference to the OverviewViewModel for managing item operations
    private val overviewViewModel: OverviewViewModel by viewModels()

    // UI elements
    private lateinit var itemNameEditText: EditText
    private lateinit var itemQuantityEditText: EditText
    private lateinit var saveItemButton: Button
    private lateinit var returnButton: ImageButton

    /**
     * Called when the activity is being created. Initializes layout, binds UI elements,
     * and sets up event listeners for saving a new item and returning to the previous screen.
     *
     * @param savedInstanceState If the activity is being re-initialized, this contains
     *                           the data from the most recent saved state.
     */
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
            // Trigger adding the item to the inventory
            addItemToInventory()
        }

        // Close the activity when the return button is clicked
        returnButton.setOnClickListener {
            finish()
        }
    }

    /**
     * Validates user input, creates a new Item object with the current date, and
     * forwards the item to the ViewModel for insertion into the inventory.
     */
    private fun addItemToInventory() {
        val itemName = itemNameEditText.text.toString().trim()
        val quantityStr = itemQuantityEditText.text.toString().trim()

        // Ensure the item name is not empty
        if (itemName.isBlank()) {
            Toast.makeText(this, "Item name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        // Parse the quantity and ensure it's valid
        val quantity = quantityStr.toIntOrNull()
        if (quantity == null || quantity < 0) {
            Toast.makeText(this, "Please enter a valid quantity", Toast.LENGTH_SHORT).show()
            return
        }

        // Format the current date
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Create a new Item with a unique ID, the provided name and quantity, and today's date
        val newItem = Item(
            id = UUID.randomUUID().toString(),
            name = itemName,
            quantity = quantity,
            dateAdded = currentDate
        )

        // Pass the new item to the ViewModel for database insertion or updating
        overviewViewModel.addOrUpdateItem(newItem)
        Toast.makeText(this, "Item added successfully", Toast.LENGTH_SHORT).show()
        finish()
    }
}

/** Reference: https://developer.android.com/reference/android/app/Activity.html?hl=en
 * Created by: Bradley Wells*/
