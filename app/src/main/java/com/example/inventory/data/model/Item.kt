package com.example.inventory.data.model

import java.util.UUID

data class Item(
    val id: String = UUID.randomUUID().toString(), // Generate a unique ID for Firestore
    val name: String = "",
    var quantity: Int = 0,
    val dateAdded: String = ""
) {
    // No-argument constructor for Firestore
    constructor() : this("", "", 0, "")

    init {
        require(quantity >= 0) { "Quantity cannot be negative" }
    }

    /**
     * Updates the quantity of the item. Ensures that the quantity remains non-negative.
     */
    fun updateQuantity(newQuantity: Int) {
        require(newQuantity >= 0) { "Quantity cannot be negative" }
        quantity = newQuantity
    }
}
