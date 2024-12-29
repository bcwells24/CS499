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
    val allItems: Flow<List<Item>> = flow {
        val items = itemsCollection.get().await().documents.mapNotNull { it.toObject<Item>() }
        emit(items)
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
            val querySnapshot = itemsCollection.get().await() // Fetch all items from Firestore
            querySnapshot.documents.mapNotNull { document ->
                document.toObject(Item::class.java) // Convert Firestore document to Item object
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList() // Return an empty list if there’s an error
        }
    }
    fun observeAllItems(): Flow<List<Item>> = callbackFlow {
        val listener = itemsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error) // Close the Flow with the error
                return@addSnapshotListener
            }

            val items = snapshot?.toObjects(Item::class.java) ?: emptyList()
            trySend(items) // Emit the updated list of items
        }

        // Clean up the listener when the Flow is canceled
        awaitClose { listener.remove() }
    }



    suspend fun getItemByName(name: String): Item? {
        return try {
            val querySnapshot = itemsCollection.whereEqualTo("name", name).get().await()
            querySnapshot.documents.firstOrNull()?.toObject()
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
    suspend fun registerUser(user: User): Boolean {
        return try {
            usersCollection.document(user.username).set(user).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun authenticateUser(username: String, password: String): Boolean {
        return try {
            val documentSnapshot = usersCollection.document(username).get().await()
            val user = documentSnapshot.toObject<User>()
            user?.password == password
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

}
