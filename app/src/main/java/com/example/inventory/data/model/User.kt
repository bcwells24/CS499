package com.example.inventory.data.model

data class User(
    val username: String = "", // Default values to avoid nulls
    val password: String = ""
) {
    // No-argument constructor for Firestore
    constructor() : this("", "")
}
