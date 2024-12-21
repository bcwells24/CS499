package com.example.inventory.data.local

import androidx.room.*
import com.example.inventory.data.model.Item
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    @Query("SELECT * FROM items")
    fun getAllItems(): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE name = :itemName LIMIT 1")
    suspend fun getItemByName(itemName: String): Item?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addItem(item: Item): Long

    @Update
    suspend fun updateItem(item: Item): Int

    @Delete
    suspend fun deleteItem(item: Item): Int
}
