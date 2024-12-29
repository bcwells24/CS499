package com.example.inventory.ui.overview

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.model.Item
import com.example.inventory.data.repository.InventoryRepository
import com.example.inventory.sms.SMSManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect

class OverviewViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = InventoryRepository()
    private val itemMap = mutableMapOf<String, Item>() // HashMap for item storage

    // Expose items as a StateFlow so UI can observe changes
    private val _allItems = MutableStateFlow<List<Item>>(emptyList())
    val allItems: StateFlow<List<Item>> get() = _allItems

    init {
        fetchAllItems()
        observeRealTimeUpdates()
    }

    private fun fetchAllItems() {
        viewModelScope.launch {
            val items = repository.getAllItems()
            itemMap.clear() // Clear and repopulate the HashMap
            items.forEach { item ->
                itemMap[item.name.lowercase()] = item // Case-insensitive keys
            }
            _allItems.value = items
        }
    }

    private fun observeRealTimeUpdates() {
        viewModelScope.launch {
            repository.observeAllItems().collect { items ->
                _allItems.value = items

                // Update itemMap for search functionality
                itemMap.clear()
                items.forEach { item ->
                    itemMap[item.name.lowercase()] = item
                }
            }
        }
    }


    fun getItemByName(itemName: String, callback: (Item?) -> Unit) {
        viewModelScope.launch {
            val item = repository.getItemByName(itemName) // This calls the suspend function in the repository
            callback(item) // Pass the result to the callback
        }
    }

    fun searchItems(query: String): List<Item> {
        val lowerCaseQuery = query.lowercase()

        // Collect all items whose names contain the query
        return itemMap.values.filter { it.name.lowercase().contains(lowerCaseQuery) }
    }


    fun addNewItem(item: Item) {
        viewModelScope.launch {
            val existingItem = repository.getItemByName(item.name)
            if (existingItem != null) {
                // If the item already exists, update its quantity
                val updatedItem = existingItem.copy(quantity = item.quantity)
                repository.updateItem(updatedItem)
            } else {
                // Otherwise, add it as a new item
                repository.addItem(item)
            }
            fetchAllItems() // Refresh the item list
        }
    }

    fun updateItemQuantity(item: Item, newQuantity: Int) {
        viewModelScope.launch {
            val updatedItem = item.copy(quantity = newQuantity)
            repository.updateItem(updatedItem)
            fetchAllItems() // Refresh the item list
        }
    }

    fun deleteItem(item: Item) {
        viewModelScope.launch {
            repository.deleteItem(item)
            fetchAllItems() // Refresh the item list
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
    private val _isAscending = MutableStateFlow(true) // True for ascending, false for descending
    val isAscending: StateFlow<Boolean> get() = _isAscending

    fun toggleSortOrder() {
        _isAscending.value = !_isAscending.value
        sortItems()
    }

    private fun sortItems() {
        val sortedList = if (_isAscending.value) {
            _allItems.value.sortedBy { it.name.lowercase() }
        } else {
            _allItems.value.sortedByDescending { it.name.lowercase() }
        }
        _allItems.value = sortedList
    }

}
