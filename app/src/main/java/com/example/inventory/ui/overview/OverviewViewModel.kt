package com.example.inventory.ui.overview

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.model.Item
import com.example.inventory.data.repository.InventoryRepository
import com.example.inventory.sms.SMSManager
import com.example.inventory.utils.heapSort // Import the heapSort utility
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OverviewViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = InventoryRepository()

    // State management
    private val _allItems = MutableStateFlow<List<Item>>(emptyList())
    val allItems: StateFlow<List<Item>> get() = _allItems

    private val itemMap = mutableMapOf<String, Item>() // For search functionality
    var isSortedAscending = true // Sort state

    init {
        observeRealTimeUpdates() // Setup real-time updates
    }

    // ----------------------------
    // Real-Time Updates
    // ----------------------------
    private fun observeRealTimeUpdates() {
        viewModelScope.launch {
            repository.observeAllItems().collect { items ->
                updateItemState(items)
            }
        }
    }

    private fun updateItemState(items: List<Item>) {
        // Convert items to mutable list for sorting
        val mutableItems = items.toMutableList()

        // Use heapSort utility for sorting
        heapSort(mutableItems) { item1, item2 ->
            if (isSortedAscending) {
                item1.name.compareTo(item2.name)
            } else {
                item2.name.compareTo(item1.name)
            }
        }

        // Update state with sorted items
        _allItems.value = mutableItems

        // Update HashMap for search functionality
        itemMap.clear()
        mutableItems.forEach { item ->
            itemMap[item.name.lowercase()] = item
        }
    }

    // ----------------------------
    // Item Management
    // ----------------------------
    fun addOrUpdateItem(item: Item) {
        viewModelScope.launch {
            val existingItem = repository.getItemByName(item.name)
            if (existingItem != null) {
                // Update item if it exists
                val updatedItem = existingItem.copy(quantity = item.quantity)
                repository.updateItem(updatedItem)
            } else {
                // Add new item if it doesn't exist
                repository.addItem(item)
            }
        }
    }

    fun deleteItem(item: Item) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }

    fun updateItemQuantity(item: Item, newQuantity: Int) {
        viewModelScope.launch {
            val updatedItem = item.copy(quantity = newQuantity)
            repository.updateItem(updatedItem)
        }
    }

    fun sendZeroQuantitySms(itemName: String) {
        val prefs = getApplication<Application>().getSharedPreferences("SMSPrefs", 0)
        val smsEnabled = prefs.getBoolean("sms_permission", false)
        val phoneNumber = prefs.getString("phone_number", "")

        if (smsEnabled && !phoneNumber.isNullOrEmpty()) {
            SMSManager.sendSMS(phoneNumber, "Item: $itemName has reached zero quantity.")
        }
    }

    // ----------------------------
    // Search and Sorting
    // ----------------------------
    fun searchItems(query: String): List<Item> {
        val lowerCaseQuery = query.lowercase()

        return itemMap.values.filter { item ->
            val words = item.name.lowercase().split(" ") // Split the item name into words
            words.any { word -> word.startsWith(lowerCaseQuery) } // Check if any word starts with the query
        }
    }

    fun toggleSortOrder() {
        isSortedAscending = !isSortedAscending
        updateItemState(_allItems.value) // Reapply sorting using heapSort
    }
    enum class SortOrder {
        ASCENDING,
        DESCENDING
    }

    private var currentSortOrder = SortOrder.ASCENDING

    fun applySortOrder() {
        _allItems.value = when (currentSortOrder) {
            SortOrder.ASCENDING -> _allItems.value.sortedBy { it.name.lowercase() }
            SortOrder.DESCENDING -> _allItems.value.sortedByDescending { it.name.lowercase() }
        }
    }




}
