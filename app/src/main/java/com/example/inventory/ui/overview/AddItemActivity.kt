package com.example.inventory.ui.overview

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.inventory.R
import com.example.inventory.data.model.Item
import java.text.SimpleDateFormat
import java.util.*

class AddItemActivity : AppCompatActivity() {

    private val overviewViewModel: OverviewViewModel by viewModels()

    private lateinit var itemNameEditText: EditText
    private lateinit var itemQuantityEditText: EditText
    private lateinit var saveItemButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_additem)

        // Initialize views
        itemNameEditText = findViewById(R.id.itemNameEditText)
        itemQuantityEditText = findViewById(R.id.itemQuantityEditText)
        saveItemButton = findViewById(R.id.saveItemButton)

        // Set the save button listener
        saveItemButton.setOnClickListener {
            addItemToInventory()
        }
    }

    private fun addItemToInventory() {
        val itemName = itemNameEditText.text.toString().trim()
        val quantityStr = itemQuantityEditText.text.toString().trim()

        // Validate inputs
        if (itemName.isBlank() || quantityStr.isBlank()) {
            Toast.makeText(this, "Please enter both name and quantity", Toast.LENGTH_SHORT).show()
            return
        }

        val quantity = quantityStr.toIntOrNull()
        if (quantity == null || quantity < 0) {
            Toast.makeText(this, "Invalid quantity", Toast.LENGTH_SHORT).show()
            return
        }

        // Format the current date
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Create or update the item
        val newItem = Item(
            id = UUID.randomUUID().toString(), // Firestore-compatible ID
            name = itemName,
            quantity = quantity,
            dateAdded = currentDate
        )

        overviewViewModel.addOrUpdateItem(newItem) // Centralized logic in ViewModel
        Toast.makeText(this, "Item saved successfully", Toast.LENGTH_SHORT).show()
        finish()
    }
}
