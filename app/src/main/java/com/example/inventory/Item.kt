package com.example.inventory

/**
 * Represents an inventory item with properties like ID, name, quantity, and date added.
 */
data class Item(
    val id: Int, // Unique identifier for the item
    var name: String, // Name of the item
    var quantity: Int, // Quantity of the item in inventory
    val dateAdded: String // Date the item was added to the inventory
)
