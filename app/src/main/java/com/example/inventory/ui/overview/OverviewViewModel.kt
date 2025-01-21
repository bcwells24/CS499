package com.example.inventory.ui.overview

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.model.Item
import com.example.inventory.data.repository.InventoryRepository
import com.example.inventory.sms.SMSManager
import com.example.inventory.utils.heapSort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * OverviewViewModel provides inventory management operations such as adding,
 * updating, deleting, and sorting items. It interacts with the repository and manages UI state.
 */
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

    /**
     * Observes real-time updates of inventory items from the repository.
     */
    private fun observeRealTimeUpdates() {
        viewModelScope.launch {
            repository.allItems.collect { items ->
                updateItemState(items)
            }
        }
    }

    /**
     * Updates the state of all items and applies sorting.
     *
     * @param items The updated list of items.
     */
    private fun updateItemState(items: List<Item>) {
        val mutableItems = items.toMutableList()

        // Use heapSort utility for sorting
        heapSort(mutableItems) { item1, item2 ->
            if (isSortedAscending) {
                item1.name.compareTo(item2.name)
            } else {
                item2.name.compareTo(item1.name)
            }
        }

        _allItems.value = mutableItems
        itemMap.clear()
        mutableItems.forEach { item ->
            itemMap[item.name.lowercase()] = item
        }
    }

    /**
     * Adds or updates an item in the inventory.
     *
     * @param item The item to be added or updated.
     */
    fun addOrUpdateItem(item: Item) {
        viewModelScope.launch {
            val existingItem = repository.getItemByName(item.name)
            if (existingItem != null) {
                val updatedItem = existingItem.copy(quantity = item.quantity)
                repository.updateItem(updatedItem)
            } else {
                repository.addItem(item)
            }
        }
    }

    /**
     * Deletes an item from the inventory.
     *
     * @param item The item to be deleted.
     */
    fun deleteItem(item: Item) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }

    /**
     * Updates the quantity of an item.
     *
     * @param item The item to be updated.
     * @param newQuantity The new quantity value.
     */
    fun updateItemQuantity(item: Item, newQuantity: Int) {
        viewModelScope.launch {
            val updatedItem = item.copy(quantity = newQuantity)
            repository.updateItem(updatedItem)
        }
    }

    /**
     * Sends an SMS notification when an item's quantity reaches zero.
     *
     * @param itemName The name of the item triggering the notification.
     */
    fun sendZeroQuantitySms(itemName: String) {
        val prefs = getApplication<Application>().getSharedPreferences("SMSPrefs", 0)
        val smsEnabled = prefs.getBoolean("sms_permission", false)
        val phoneNumber = prefs.getString("phone_number", "")

        if (smsEnabled && !phoneNumber.isNullOrEmpty()) {
            SMSManager.sendSMS(phoneNumber, "Item: $itemName has reached zero quantity.")
        }
    }

    /**
     * Filters items based on a search query.
     *
     * @param query The search term.
     * @return A list of matching items.
     */
    fun searchItems(query: String): List<Item> {
        val lowerCaseQuery = query.lowercase()

        return itemMap.values.filter { item ->
            val words = item.name.lowercase().split(" ")
            words.any { word -> word.startsWith(lowerCaseQuery) }
        }
    }

    /**
     * Toggles the sorting order of the items and updates the state.
     */
    fun toggleSortOrder() {
        isSortedAscending = !isSortedAscending
        updateItemState(_allItems.value)
    }
}

/** Reference: https://developer.android.com/reference/kotlin/androidx/lifecycle/ViewModel */
