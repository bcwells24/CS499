package com.example.inventory.data.repository

import com.example.inventory.data.local.InventoryDao
import com.example.inventory.data.local.UserDao
import com.example.inventory.data.model.Item
import com.example.inventory.data.model.User
import kotlinx.coroutines.flow.Flow

class InventoryRepository(
    private val inventoryDao: InventoryDao,
    private val userDao: UserDao
) {

    // Flow of all items for real-time updates
    val allItems: Flow<List<Item>> = inventoryDao.getAllItems()

    suspend fun addItem(item: Item): Boolean {
        return inventoryDao.addItem(item) != -1L
    }

    suspend fun getItemByName(name: String): Item? {
        return inventoryDao.getItemByName(name)
    }

    suspend fun updateItem(item: Item): Boolean {
        return inventoryDao.updateItem(item) > 0
    }

    suspend fun deleteItem(item: Item): Boolean {
        return inventoryDao.deleteItem(item) > 0
    }

    // User operations
    suspend fun registerUser(user: User): Boolean {
        return userDao.registerUser(user) != -1L
    }

    suspend fun authenticateUser(username: String, password: String): Boolean {
        return userDao.authenticateUser(username, password) != null
    }
}
