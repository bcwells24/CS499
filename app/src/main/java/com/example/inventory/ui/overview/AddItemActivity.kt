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

        itemNameEditText = findViewById(R.id.itemNameEditText)
        itemQuantityEditText = findViewById(R.id.itemQuantityEditText)
        saveItemButton = findViewById(R.id.saveItemButton)

        saveItemButton.setOnClickListener {
            addItemToInventory()
        }
    }

    private fun addItemToInventory() {
        val itemName = itemNameEditText.text.toString()
        val quantityStr = itemQuantityEditText.text.toString()

        if (itemName.isBlank() || quantityStr.isBlank()) {
            Toast.makeText(this, "Please enter both name and quantity", Toast.LENGTH_SHORT).show()
            return
        }

        val quantity = quantityStr.toIntOrNull()
        if (quantity == null) {
            Toast.makeText(this, "Invalid quantity", Toast.LENGTH_SHORT).show()
            return
        }

        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // We can check if item already exists by name, or just insert.
        // For demonstration, we’ll just add the item.
        val newItem = Item(name = itemName, quantity = quantity, dateAdded = currentDate)
        overviewViewModel.viewModelScope.launchWhenStarted {
            val existingItem = overviewViewModel.allItems.value.find { it.name == itemName }
            if (existingItem != null) {
                overviewViewModel.updateItemQuantity(existingItem, existingItem.quantity + quantity)
                Toast.makeText(this@AddItemActivity, "Item quantity updated", Toast.LENGTH_SHORT).show()
            } else {
                // In a real scenario, you'd call repository.addItem(...) from the VM
                // to keep consistent with the MVVM approach
                overviewViewModel.updateItemQuantity(newItem, newItem.quantity)
                Toast.makeText(this@AddItemActivity, "Item added successfully", Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }
}
