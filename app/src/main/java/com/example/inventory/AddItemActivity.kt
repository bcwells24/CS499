package com.example.inventory

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class AddItemActivity : AppCompatActivity() {

    // Declare EditText fields for item name and quantity input
    private lateinit var itemNameEditText: EditText
    private lateinit var itemQuantityEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_additem)

        // Initialize UI components (EditText fields and Button)
        itemNameEditText = findViewById(R.id.itemNameEditText)
        itemQuantityEditText = findViewById(R.id.itemQuantityEditText)
        val saveItemButton: Button = findViewById(R.id.saveItemButton)

        // Set up listener for the save button click event
        saveItemButton.setOnClickListener {
            addItemToInventory()
        }
    }

    // Method to handle adding the item to the inventory
    private fun addItemToInventory() {
        // Get user input for item name and quantity
        val itemName = itemNameEditText.text.toString()
        val itemQuantityStr = itemQuantityEditText.text.toString()
        val itemDate = getCurrentDate() // Get current date

        // Validate input: ensure both item name and quantity are entered
        if (itemName.isEmpty() || itemQuantityStr.isEmpty()) {
            Toast.makeText(this, "Please enter both item name and quantity", Toast.LENGTH_SHORT).show()
            return
        }

        val itemQuantity: Int = try {
            // Try to convert the quantity input to an integer
            itemQuantityStr.toInt()
        } catch (e: NumberFormatException) {
            // Show an error message if the quantity is not a valid number
            Toast.makeText(this, "Please enter a valid quantity", Toast.LENGTH_SHORT).show()
            return
        }

        // Initialize DatabaseHelper to interact with the database
        val dbHelper = DatabaseHelper(this)

        // Check if the item already exists in the database
        val existingItem = dbHelper.getItemByName(itemName) // You will need to implement getItemByName method in DatabaseHelper

        val success = if (existingItem != null) {
            // If the item exists, update its quantity
            dbHelper.updateItemQuantity(existingItem.id, itemQuantity).also {
                Toast.makeText(this, "Item quantity updated successfully", Toast.LENGTH_SHORT).show()
            }
        } else {
            // If the item doesn't exist, add a new item
            dbHelper.addItem(itemName, itemQuantity, itemDate).also {
                Toast.makeText(this, "Item added successfully", Toast.LENGTH_SHORT).show()
            }
        }

        if (success) {
            // If the operation is successful, navigate back to the OverviewActivity
            val intent = Intent(this, OverviewActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // Show an error message if the operation fails
            Toast.makeText(this, "Failed to add or update item", Toast.LENGTH_SHORT).show()
        }

        // Go back to the OverviewActivity regardless
        val intent = Intent(this, OverviewActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Method to get the current date in "yyyy-MM-dd" format
    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance() // Get the current date
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Set date format
        return sdf.format(calendar.time) // Return formatted date
    }
}
