package com.example.inventory.data.repository

import com.example.inventory.data.model.Item
import com.example.inventory.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose

/**
 * InventoryRepository handles database operations for inventory items and user authentication.
 * It interfaces with Firestore to perform CRUD operations and provides real-time updates using Kotlin flows.
 */
class InventoryRepository {

    // Firestore instance for accessing collections
    private val firestore = FirebaseFirestore.getInstance()

    // Firestore collection references
    private val itemsCollection = firestore.collection("items")
    private val usersCollection = firestore.collection("users")

    /**
     * A Flow that emits a list of all inventory items, providing real-time updates.
     */
    val allItems: Flow<List<Item>> = callbackFlow {
        val listener = itemsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error) // Close the flow if an error occurs
                return@addSnapshotListener
            }

            // Convert Firestore documents into a list of Item objects
            val items = snapshot?.toObjects(Item::class.java) ?: emptyList()
            trySend(items) // Emit the items through the flow
        }

        // Remove the listener when the flow is closed
        awaitClose { listener.remove() }
    }

    /**
     * Adds a new item to the Firestore database.
     *
     * @param item The item to be added.
     * @return true if the item was added successfully, false otherwise.
     */
    suspend fun addItem(item: Item): Boolean {
        return try {
            itemsCollection.add(item).await() // Firestore auto-generates the document ID
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Retrieves an item from the Firestore database by its name.
     *
     * @param name The name of the item to retrieve.
     * @return The matching item, or null if not found.
     */
    suspend fun getItemByName(name: String): Item? {
        return try {
            val querySnapshot = itemsCollection.whereEqualTo("name", name).get().await()
            querySnapshot.documents.firstOrNull()?.toObject(Item::class.java) // Convert the first matching document to an Item
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Updates an existing item in the Firestore database.
     *
     * @param item The updated item object.
     * @return true if the item was updated successfully, false otherwise.
     */
    suspend fun updateItem(item: Item): Boolean {
        return try {
            val querySnapshot = itemsCollection.whereEqualTo("name", item.name).get().await()
            if (querySnapshot.documents.isNotEmpty()) {
                val documentId = querySnapshot.documents.first().id // Get Firestore's document ID
                itemsCollection.document(documentId).set(item).await() // Update the document
                true
            } else {
                false // Item not found
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Deletes an item from the Firestore database.
     *
     * @param item The item to delete.
     * @return true if the item was deleted successfully, false otherwise.
     */
    suspend fun deleteItem(item: Item): Boolean {
        return try {
            val querySnapshot = itemsCollection.whereEqualTo("name", item.name).get().await()
            if (querySnapshot.documents.isNotEmpty()) {
                val documentId = querySnapshot.documents.first().id // Get Firestore's document ID
                itemsCollection.document(documentId).delete().await() // Delete the document
                true
            } else {
                false // Item not found
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Registers a new user in the Firestore database with an encrypted password.
     *
     * @param username The username of the user.
     * @param password The plaintext password of the user.
     * @return true if the user was registered successfully, false otherwise.
     */
    suspend fun registerUser(username: String, password: String): Boolean {
        return try {
            // Encrypt the password before saving
            val encryptedPassword = User.encryptPassword(password)

            // Create a User object with the encrypted password
            val user = User(username = username, encryptedPassword = encryptedPassword)

            // Save the User object to Firestore
            usersCollection.document(username).set(user).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Authenticates a user by verifying the provided password with the stored encrypted password.
     *
     * @param username The username of the user.
     * @param password The plaintext password to validate.
     * @return true if authentication is successful, false otherwise.
     */
    suspend fun authenticateUser(username: String, password: String): Boolean {
        return try {
            val documentSnapshot = usersCollection.document(username).get().await()
            val user = documentSnapshot.toObject(User::class.java)

            // Validate the password by decrypting the stored encrypted password
            user?.let {
                val decryptedPassword = User.decryptPassword(it.encryptedPassword)
                decryptedPassword == password // Return true if the passwords match
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

/** Reference: https://firebase.google.com/docs/firestore/quickstart */