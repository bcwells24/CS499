package com.example.inventory.ui.overview

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
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
import java.text.SimpleDateFormat
import java.util.*

class OverviewViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: InventoryRepository

    // Expose items as a StateFlow so UI can observe changes
    val allItems: StateFlow<List<Item>>

    init {
        val db = InventoryDatabase.getDatabase(application)
        repository = InventoryRepository(db.inventoryDao(), db.userDao())

        // Convert the Flow<List<Item>> from the repository to a StateFlow for easy UI consumption
        allItems = repository.allItems
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    }

    fun updateItemQuantity(item: Item, newQuantity: Int) {
        viewModelScope.launch {
            // Directly modify the item quantity, then update DB
            val updatedItem = item.copy(quantity = newQuantity)
            repository.updateItem(updatedItem)
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
