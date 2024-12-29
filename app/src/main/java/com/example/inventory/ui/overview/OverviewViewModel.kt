package com.example.inventory.ui.overview

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.local.InventoryDatabase
import com.example.inventory.data.model.Item
import com.example.inventory.data.repository.InventoryRepository
import com.example.inventory.sms.SMSManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OverviewViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: InventoryRepository
    private val itemMap = mutableMapOf<String, Item>() // HashMap for item storage
    // Expose items as a StateFlow so UI can observe changes
    val allItems: StateFlow<List<Item>>

    init {
        val db = InventoryDatabase.getDatabase(application)
        repository = InventoryRepository(db.inventoryDao(), db.userDao())

        // Convert the Flow<List<Item>> from the repository to a StateFlow for easy UI consumption
        allItems = repository.allItems
            .map { items ->
                // Populate the HashMap whenever the items list is updated
                itemMap.clear()
                items.forEach { item ->
                    itemMap[item.name.lowercase()] = item // Case-insensitive keys
                }
                items
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    }
    fun searchItems(query: String): List<Item> {
        val lowerCaseQuery = query.lowercase()
        // Check if the query matches exactly
        val exactMatch = itemMap[lowerCaseQuery]
        if (exactMatch != null) return listOf(exactMatch)

        // Perform partial matching if no exact match
        return itemMap.keys.filter { it.contains(lowerCaseQuery) }.mapNotNull { itemMap[it] }
    }

    fun addNewItem(item: Item) {
        viewModelScope.launch {
            repository.addItem(item) // Use repository to insert the item
        }
    }


    fun updateItemQuantity(item: Item, newQuantity: Int) {
        viewModelScope.launch {
            // Directly modify the item quantity, then update DB
            val updatedItem = item.copy(quantity = newQuantity)
            repository.updateItem(updatedItem)
        }
    }

    fun deleteItem(item: Item) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }

    fun getItemByName(itemName: String, callback: (Item?) -> Unit) {
        viewModelScope.launch {
            val item = repository.getItemByName(itemName) // This calls the suspend function
            callback(item) // Pass the result to the callback
        }
    }



    fun sendZeroQuantitySms(itemName: String) {
        // Get phone number and SMS permission from SharedPreferences (simplified)
        val prefs = getApplication<Application>().getSharedPreferences("SMSPrefs", 0)
        val smsEnabled = prefs.getBoolean("sms_permission", false)
        val phoneNumber = prefs.getString("phone_number", "")

        if (smsEnabled && !phoneNumber.isNullOrEmpty()) {
            SMSManager.sendSMS(phoneNumber, "Item: $itemName has reached zero quantity.")
        }
    }
}
