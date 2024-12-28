package com.example.inventory.ui.overview

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.inventory.R
import com.example.inventory.data.model.Item
import kotlinx.coroutines.launch
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

        itemNameEditText = findViewById(R.id.itemNameEditText)
        itemQuantityEditText = findViewById(R.id.itemQuantityEditText)
        saveItemButton = findViewById(R.id.saveItemButton)

        saveItemButton.setOnClickListener {
            addItemToInventory()
        }
    }

    private fun addItemToInventory() {
        val itemName = itemNameEditText.text.toString().trim()
        val quantityStr = itemQuantityEditText.text.toString().trim()

        if (itemName.isBlank() || quantityStr.isBlank()) {
            Toast.makeText(this, "Please enter both name and quantity", Toast.LENGTH_SHORT).show()
            return
        }

        val quantity = quantityStr.toIntOrNull()
        if (quantity == null || quantity < 0) {
            Toast.makeText(this, "Invalid quantity", Toast.LENGTH_SHORT).show()
            return
        }

        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        lifecycleScope.launch {
            overviewViewModel.getItemByName(itemName) { existingItem ->
                if (existingItem != null) {
                    // Replace the existing quantity with the new quantity
                    val updatedItem = existingItem.copy(quantity = quantity)
                    overviewViewModel.updateItemQuantity(updatedItem, quantity)
                    Toast.makeText(this@AddItemActivity, "Item quantity updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    // Add a new item if it doesn't already exist
                    val newItem = Item(name = itemName, quantity = quantity, dateAdded = currentDate)
                    overviewViewModel.addNewItem(newItem)
                    Toast.makeText(this@AddItemActivity, "Item added successfully", Toast.LENGTH_SHORT).show()
                }
                finish()
            }
        }
    }


}

