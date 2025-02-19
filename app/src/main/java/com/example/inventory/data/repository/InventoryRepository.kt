package com.example.inventory.data.repository

import com.example.inventory.data.model.Item
import com.example.inventory.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose

/**
 * InventoryRepository handles database operations for inventory items and user data (authentication).
 * It uses Firebase Firestore to perform CRUD actions on items and also provides methods to register
 * and authenticate users. Real-time updates for inventory items are managed via Kotlin Flows.
 */
class InventoryRepository {

    // Access the Firestore database instance
    private val firestore = FirebaseFirestore.getInstance()

    // Define references to the "items" and "users" collections in Firestore
    private val itemsCollection = firestore.collection("items")
    private val usersCollection = firestore.collection("users")

    /**
     * A Kotlin Flow that emits the list of all inventory items, providing real-time updates.
     * This flow listens to snapshot changes in the "items" Firestore collection.
     */
    val allItems: Flow<List<Item>> = callbackFlow {
        val listener = itemsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Close the flow if an error occurs
                close(error)
                return@addSnapshotListener
            }

            // Convert Firestore documents to a list of Item objects
            val items = snapshot?.toObjects(Item::class.java) ?: emptyList()
            // Emit the items through the flow
            trySend(items)
        }

        // Remove the listener when the flow is closed
        awaitClose { listener.remove() }
    }

    /**
     * Adds a new item document to the Firestore database.
     *
     * @param item The item to be added to Firestore.
     * @return true if the item was added successfully, false otherwise.
     */
    suspend fun addItem(item: Item): Boolean {
        return try {
            // Firestore automatically generates a document ID when adding new data
            itemsCollection.add(item).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Retrieves an item from Firestore by its name.
     *
     * @param name The name of the item to look up.
     * @return The matching Item object, or null if no item was found.
     */
    suspend fun getItemByName(name: String): Item? {
        return try {
            val querySnapshot = itemsCollection.whereEqualTo("name", name).get().await()
            // Convert the first matching document to an Item, if present
            querySnapshot.documents.firstOrNull()?.toObject(Item::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Updates an existing item in Firestore by matching its name field.
     *
     * @param item The updated Item object containing new field values.
     * @return true if the item was updated successfully, false otherwise.
     */
    suspend fun updateItem(item: Item): Boolean {
        return try {
            val querySnapshot = itemsCollection.whereEqualTo("name", item.name).get().await()
            if (querySnapshot.documents.isNotEmpty()) {
                val documentId = querySnapshot.documents.first().id
                // Overwrite the existing document with the new Item data
                itemsCollection.document(documentId).set(item).await()
                true
            } else {
                // No document was found matching the given item name
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Deletes an existing item from Firestore.
     *
     * @param item The Item object to delete (matched by name).
     * @return true if the item was successfully deleted, false otherwise.
     */
    suspend fun deleteItem(item: Item): Boolean {
        return try {
            val querySnapshot = itemsCollection.whereEqualTo("name", item.name).get().await()
            if (querySnapshot.documents.isNotEmpty()) {
                val documentId = querySnapshot.documents.first().id
                // Delete the document from Firestore
                itemsCollection.document(documentId).delete().await()
                true
            } else {
                // The specified item was not found in the collection
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Registers a new user in Firestore by encrypting their password before saving.
     *
     * @param username The user's chosen username.
     * @param password The plaintext password that will be encrypted.
     * @return true if the user was successfully registered, false otherwise.
     */
    suspend fun registerUser(username: String, password: String): Boolean {
        return try {
            // Encrypt the password before saving it to Firestore
            val encryptedPassword = User.encryptPassword(password)

            // Create a User object with the encrypted password
            val user = User(username = username, encryptedPassword = encryptedPassword)

            // Store the User object in Firestore, using the username as the document ID
            usersCollection.document(username).set(user).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Authenticates a user by verifying the provided plaintext password against the stored (encrypted) password.
     *
     * @param username The username of the user attempting to log in.
     * @param password The plaintext password provided by the user.
     * @return true if authentication is successful (password match), false otherwise.
     */
    suspend fun authenticateUser(username: String, password: String): Boolean {
        return try {
            // Retrieve the user document from Firestore
            val documentSnapshot = usersCollection.document(username).get().await()
            val user = documentSnapshot.toObject(User::class.java)

            // Decrypt the stored password to verify
            user?.let {
                val decryptedPassword = User.decryptPassword(it.encryptedPassword)
                // Return true if the passwords match
                decryptedPassword == password
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

/** Reference: https://firebase.google.com/docs/firestore/quickstart
  * Created by: Bradley Wells */
