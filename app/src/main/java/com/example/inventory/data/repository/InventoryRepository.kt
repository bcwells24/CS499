package com.example.inventory.data.repository

import com.example.inventory.data.model.Item
import com.example.inventory.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose

class InventoryRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val itemsCollection = firestore.collection("items")
    private val usersCollection = firestore.collection("users")

    // Flow of all items for real-time updates
    val allItems: Flow<List<Item>> = callbackFlow {
        val listener = itemsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val items = snapshot?.toObjects(Item::class.java) ?: emptyList()
            trySend(items)
        }

        awaitClose { listener.remove() }
    }

    suspend fun addItem(item: Item): Boolean {
        return try {
            itemsCollection.add(item).await() // Firestore generates the document ID
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getAllItems(): List<Item> {
        return try {
            val querySnapshot = itemsCollection.get().await()
            querySnapshot.toObjects(Item::class.java) // Simplified using KTX API
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getItemByName(name: String): Item? {
        return try {
            val querySnapshot = itemsCollection.whereEqualTo("name", name).get().await()
            querySnapshot.documents.firstOrNull()?.toObject(Item::class.java) // Use KTX API
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

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

    // User operations
    suspend fun registerUser(username: String, password: String): Boolean {
        return try {
            // Encrypt the password
            val encryptedPassword = User.encryptPassword(password)

            // Create the User instance with the encrypted password
            val user = User(username = username, encryptedPassword = encryptedPassword)

            // Save the user object to Firestore
            usersCollection.document(username).set(user).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    suspend fun authenticateUser(username: String, password: String): Boolean {
        return try {
            val documentSnapshot = usersCollection.document(username).get().await()
            val user = documentSnapshot.toObject(User::class.java)

            // Validate the password by decrypting the stored encrypted password
            user?.let {
                val decryptedPassword = User.decryptPassword(it.encryptedPassword)
                decryptedPassword == password
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


}
