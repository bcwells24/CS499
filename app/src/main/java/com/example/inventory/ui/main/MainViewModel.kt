package com.example.inventory.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.local.InventoryDatabase
import com.example.inventory.data.repository.InventoryRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: InventoryRepository

    init {
        val db = InventoryDatabase.getDatabase(application)
        repository = InventoryRepository(db.inventoryDao(), db.userDao())
    }

    fun authenticateUser(username: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.authenticateUser(username, password)
            onResult(success)
        }
    }
}
