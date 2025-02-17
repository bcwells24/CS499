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
 * OverviewViewModel manages the core inventory operations such as adding, updating,
 * deleting, and sorting items, and provides data to the UI via a StateFlow. It
 * also handles sending notifications when an item’s quantity reaches zero.
 *
 * @param application The application context, provided to the AndroidViewModel.
 */
class OverviewViewModel(application: Application) : AndroidViewModel(application) {

    // Reference to the repository that handles Firestore interactions
    private val repository = InventoryRepository()

    // Backing state for all items in the inventory
    private val _allItems = MutableStateFlow<List<Item>>(emptyList())
    /**
     * A StateFlow that emits the current list of items whenever changes occur.
     */
    val allItems: StateFlow<List<Item>> get() = _allItems

    // A map for quick name-based lookups (used in search functionality)
    private val itemMap = mutableMapOf<String, Item>()

    // Tracks the current sort order of items (ascending vs. descending by name)
    var isSortedAscending = true

    init {
        // Begin observing real-time updates from Firestore once this ViewModel is created
        observeRealTimeUpdates()
    }

    /**
     * Observes real-time updates from the repository's Flow of items. Whenever new items
     * are received, updates the ViewModel’s state accordingly.
     */
    private fun observeRealTimeUpdates() {
        viewModelScope.launch {
            repository.allItems.collect { items ->
                // Update the internal state with the new list
                updateItemState(items)
            }
        }
    }

    /**
     * Sorts the incoming list of items, updates the public StateFlow, and refreshes the
     * itemMap used for search. Sorting is done via the heapSort utility method.
     *
     * @param items The latest list of items from Firestore.
     */
    private fun updateItemState(items: List<Item>) {
        val mutableItems = items.toMutableList()

        // Use a custom heap sort to order items by name
        heapSort(mutableItems) { item1, item2 ->
            if (isSortedAscending) {
                item1.name.compareTo(item2.name)
            } else {
                item2.name.compareTo(item1.name)
            }
        }

        // Update the StateFlow with the newly sorted items
        _allItems.value = mutableItems

        // Rebuild the itemMap for search
        itemMap.clear()
        mutableItems.forEach { item ->
            itemMap[item.name.lowercase()] = item
        }
    }

    /**
     * Adds a new item to the inventory or updates it if it already exists.
     *
     * @param item The item to be inserted or updated in Firestore.
     */
    fun addOrUpdateItem(item: Item) {
        viewModelScope.launch {
            val existingItem = repository.getItemByName(item.name)
            if (existingItem != null) {
                // If the item already exists, update its quantity
                val updatedItem = existingItem.copy(quantity = item.quantity)
                repository.updateItem(updatedItem)
            } else {
                // Otherwise, add a new item to the database
                repository.addItem(item)
            }
        }
    }

    /**
     * Removes the specified item from the Firestore collection.
     *
     * @param item The item to be deleted.
     */
    fun deleteItem(item: Item) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }

    /**
     * Updates the quantity of an existing item in the inventory.
     *
     * @param item The item whose quantity needs to be updated.
     * @param newQuantity The new quantity value to set.
     */
    fun updateItemQuantity(item: Item, newQuantity: Int) {
        viewModelScope.launch {
            val updatedItem = item.copy(quantity = newQuantity)
            repository.updateItem(updatedItem)
        }
    }

    /**
     * Sends an SMS notification if the user has granted permission and provided a phone number.
     * Called when an item's quantity drops to zero.
     *
     * @param itemName The name of the item that triggered the SMS alert.
     */
    fun sendZeroQuantitySms(itemName: String) {
        // Retrieve user preferences from SharedPreferences
        val prefs = getApplication<Application>().getSharedPreferences("SMSPrefs", 0)
        val smsEnabled = prefs.getBoolean("sms_permission", false)
        val phoneNumber = prefs.getString("phone_number", "")

        // Only send SMS if permission is enabled and a phone number is set
        if (smsEnabled && !phoneNumber.isNullOrEmpty()) {
            SMSManager.sendSMS(phoneNumber, "Item: $itemName has reached zero quantity.")
        }
    }

    /**
     * Filters the list of items based on a search query. Items match if their name contains
     * the query as a starting substring of any word.
     *
     * @param query The search term.
     * @return A list of items whose names match the query.
     */
    fun searchItems(query: String): List<Item> {
        val lowerCaseQuery = query.lowercase()
        return itemMap.values.filter { item ->
            val words = item.name.lowercase().split(" ")
            words.any { word -> word.startsWith(lowerCaseQuery) }
        }
    }

    /**
     * Toggles between ascending and descending name sorting for items and updates the
     * _allItems flow accordingly.
     */
    fun toggleSortOrder() {
        isSortedAscending = !isSortedAscending
        // Reapply sorting to the current list of items
        updateItemState(_allItems.value)
    }
}

/** Reference: https://developer.android.com/reference/kotlin/androidx/lifecycle/ViewModel */
